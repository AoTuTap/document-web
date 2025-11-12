# SOAP to REST Bridge Sample

Đây là ví dụ Spring Boot minh họa cách lắng nghe SOAP service và chuyển tiếp payload sang REST API bên ngoài.

## Chạy ứng dụng

```bash
mvn spring-boot:run
```

Ứng dụng xuất bản SOAP endpoint tại `http://localhost:8080/ws`. WSDL nằm ở `http://localhost:8080/ws/bridge.wsdl`.

## Gửi request thử nghiệm

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:sch="http://example.com/soaprestbridge/schema">
   <soapenv:Header/>
   <soapenv:Body>
      <sch:BridgeRequest>
         <sch:correlationId>demo-001</sch:correlationId>
         <sch:targetPath>/post</sch:targetPath>
         <sch:payload>{"message":"Xin chao"}</sch:payload>
      </sch:BridgeRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

Ứng dụng sẽ chuyển payload JSON tới `https://httpbin.org/post` (cấu hình trong `application.yml`) và trả về phản hồi REST dưới dạng SOAP response.

## Cấu hình

Các thông số chính được thiết lập trong `application.yml`:

- `bridge.rest-base-url`: REST API đích.
- `bridge.default-target-path`: Đường dẫn mặc định nếu request không gửi `targetPath`.
- `bridge.connect-timeout` và `bridge.read-timeout`: timeout (ms) cho RestTemplate.
