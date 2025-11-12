# Xây dựng Spring Boot SOAP service chuyển tiếp sang REST API

Tài liệu này mô tả cách cấu hình một ứng dụng Spring Boot lắng nghe SOAP service và gọi RESTful API sang hệ thống khác. Các bước bao gồm chuẩn bị dependencies, tạo contract SOAP, triển khai endpoint, và ánh xạ lời gọi sang REST client.

## 1. Chuẩn bị dự án

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Web để gọi REST API -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Web Services để tạo SOAP endpoint -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web-services</artifactId>
    </dependency>

    <!-- JAXB để bind XML sang Java -->
    <dependency>
        <groupId>org.glassfish.jaxb</groupId>
        <artifactId>jaxb-runtime</artifactId>
    </dependency>

    <!-- Optional: Lombok, logging, v.v. -->
</dependencies>
```

Khai báo plugin `jaxb2-maven-plugin` để sinh mã từ schema (nếu cần).

## 2. Định nghĩa contract SOAP

1. Viết file `src/main/resources/wsdl/bridge.xsd` mô tả request/response:

```xml
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           targetNamespace="http://example.com/bridge"
           elementFormDefault="qualified">
    <xs:element name="BridgeRequest">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="customerId" type="xs:string"/>
                <xs:element name="payload" type="xs:string"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <xs:element name="BridgeResponse">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="status" type="xs:string"/>
                <xs:element name="message" type="xs:string" minOccurs="0"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>
</xs:schema>
```

2. Sử dụng `jaxb2-maven-plugin` để sinh các lớp request/response.

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>jaxb2-maven-plugin</artifactId>
    <version>2.5.0</version>
    <executions>
        <execution>
            <goals>
                <goal>xjc</goal>
            </goals>
            <configuration>
                <schemaDirectory>${project.basedir}/src/main/resources/wsdl</schemaDirectory>
                <packageName>com.example.bridge.wsdl</packageName>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 3. Cấu hình Spring WS

Tạo file `WebServiceConfig`.

```java
@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "bridge")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema schema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("BridgePort");
        definition.setLocationUri("/ws");
        definition.setTargetNamespace("http://example.com/bridge");
        definition.setSchema(schema);
        return definition;
    }

    @Bean
    public XsdSchema bridgeSchema() {
        return new SimpleXsdSchema(new ClassPathResource("wsdl/bridge.xsd"));
    }
}
```

## 4. Tạo SOAP Endpoint

```java
@Endpoint
@RequiredArgsConstructor
public class BridgeEndpoint {

    private static final String NAMESPACE_URI = "http://example.com/bridge";
    private final BridgeService bridgeService;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "BridgeRequest")
    @ResponsePayload
    public BridgeResponse handleRequest(@RequestPayload BridgeRequest request) {
        return bridgeService.process(request);
    }
}
```

## 5. Gọi RESTful API từ service layer

```java
@Service
@RequiredArgsConstructor
public class BridgeService {

    private final RestTemplate restTemplate; // hoặc WebClient

    public BridgeResponse process(BridgeRequest request) {
        RestPayload payload = RestPayload.builder()
                .customerId(request.getCustomerId())
                .data(request.getPayload())
                .build();

        ResponseEntity<RestResult> response = restTemplate.postForEntity(
                "https://remote-system/api/endpoint", payload, RestResult.class);

        BridgeResponse bridgeResponse = new BridgeResponse();
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            bridgeResponse.setStatus("SUCCESS");
            bridgeResponse.setMessage(response.getBody().getMessage());
        } else {
            bridgeResponse.setStatus("FAILED");
            bridgeResponse.setMessage("REST call failed: " + response.getStatusCode());
        }
        return bridgeResponse;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
```

Có thể dùng `WebClient` nếu muốn lập trình reactive và xử lý timeout/retry tốt hơn.

## 6. Logging và xử lý lỗi

- Log request SOAP và response (ẩn thông tin nhạy cảm).
- Bao bọc lỗi khi REST API không phản hồi: dùng `try/catch` trả về trạng thái `FAILED`.
- Có thể cấu hình `ClientHttpRequestInterceptor` cho RestTemplate để thêm header hoặc auth token.

## 7. Bảo mật

- Thêm `SecurityConfig` nếu SOAP endpoint cần xác thực (WS-Security / Basic Auth / Token).
- Nếu REST API yêu cầu token, cấu hình component lấy token và gắn vào header.

## 8. Kiểm thử

- Viết `@Endpoint` test với `MockWebServiceClient` để mock SOAP request.
- Sử dụng WireMock hoặc MockRestServiceServer để mock REST API.

## 9. Triển khai

- Đảm bảo mở port `/ws` trong application server.
- Cập nhật tài liệu WSDL cho bên gọi SOAP.
- Giám sát lỗi bằng metrics (Micrometer) và logging.

Tài liệu này cung cấp khung tổng quan; điều chỉnh lại tên package, trường dữ liệu, và xử lý nghiệp vụ cụ thể theo nhu cầu hệ thống của bạn.
