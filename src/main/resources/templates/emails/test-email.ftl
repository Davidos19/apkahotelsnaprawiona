<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Test Email</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
        }
        .test-container {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            border-radius: 10px;
            text-align: center;
        }
        .success-icon {
            font-size: 60px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
<div class="test-container">
    <div class="success-icon">✅</div>
    <h1>Email System Test</h1>
    <p>${testMessage}</p>
    <p><strong>Data wysłania:</strong> ${.now}</p>
</div>
</body>
</html>