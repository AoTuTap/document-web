import os
from flask import Flask, render_template, request, redirect, url_for, session, send_from_directory, flash

app = Flask(__name__)
app.secret_key = 'change_this_secret_key'

# Simple in-memory user store
USERS = {
    'admin': 'password'
}

DOCUMENT_FOLDER = os.path.join(os.path.dirname(__file__), 'documents')
os.makedirs(DOCUMENT_FOLDER, exist_ok=True)


def is_logged_in():
    return session.get('logged_in')


@app.route('/')
def index():
    if not is_logged_in():
        return redirect(url_for('login'))
    return render_template('search.html')


@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        if USERS.get(username) == password:
            session['logged_in'] = True
            session['username'] = username
            return redirect(url_for('index'))
        flash('Invalid credentials')
    return render_template('login.html')


@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('login'))


@app.route('/search')
def search():
    if not is_logged_in():
        return redirect(url_for('login'))
    query = request.args.get('q', '').lower()
    results = []
    if query:
        for filename in os.listdir(DOCUMENT_FOLDER):
            if query in filename.lower():
                results.append(filename)
    return render_template('search.html', query=query, results=results)


@app.route('/download/<path:filename>')
def download(filename):
    if not is_logged_in():
        return redirect(url_for('login'))
    return send_from_directory(DOCUMENT_FOLDER, filename, as_attachment=True)


if __name__ == '__main__':
    app.run(debug=True)
