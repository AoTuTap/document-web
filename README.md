# Document Web

A simple Flask web application that allows users to log in, search for documents, and download them.

## Features

- User login with session management.
- Search for document names stored in the `documents/` directory.
- Click a search result to download the file.

## Setup

```bash
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

## Running

```bash
python app.py
```

The application will be available at `http://localhost:5000`.

Use the default credentials:

- **Username:** `admin`
- **Password:** `password`

Add your documents to the `documents/` directory. They will automatically be included in search results.
