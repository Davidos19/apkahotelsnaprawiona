<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Witamy w systemie</title>
    <style>
        body {
            font-family: 'Arial', sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f4f4f4;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 0 20px rgba(0,0,0,0.1);
        }
        .header {
            text-align: center;
            border-bottom: 3px solid #28a745;
            padding-bottom: 20px;
            margin-bottom: 30px;
        }
        .welcome-badge {
            background: #28a745;
            color: white;
            padding: 15px 30px;
            border-radius: 50px;
            display: inline-block;
            margin: 20px 0;
            font-weight: bold;
            font-size: 18px;
        }
        .features {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin: 30px 0;
        }
        .feature {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
        }
        .feature-icon {
            font-size: 40px;
            margin-bottom: 10px;
        }
        .cta-button {
            background: #007bff;
            color: white;
            padding: 15px 40px;
            border: none;
            border-radius: 25px;
            text-decoration: none;
            display: inline-block;
            margin: 20px 0;
            font-weight: bold;
            font-size: 16px;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>🎉 Witamy w Hotel Booking System!</h1>
        <div class="welcome-badge">Witaj ${userName}!</div>
    </div>

    <p>Gratulacje! Twoje konto zostało pomyślnie utworzone.</p>

    <p>Teraz możesz korzystać ze wszystkich funkcji naszego systemu rezerwacji hotelowych.</p>

    <div class="features">
        <div class="feature">
            <div class="feature-icon">🔍</div>
            <h4>Szukaj hoteli</h4>
            <p>Znajdź idealny hotel dla siebie</p>
        </div>
        <div class="feature">
            <div class="feature-icon">📅</div>
            <h4>Rezerwuj pokoje</h4>
            <p>Szybka i łatwa rezerwacja</p>
        </div>
        <div class="feature">
            <div class="feature-icon">📧</div>
            <h4>Powiadomienia</h4>
            <p>Otrzymuj aktualizacje email</p>
        </div>
        <div class="feature">
            <div class="feature-icon">⭐</div>
            <h4>Oceniaj hotele</h4>
            <p>Dziel się opiniami</p>
        </div>
    </div>

    <div style="text-align: center;">
        <a href="#" class="cta-button">Zacznij przeglądać hotele</a>
    </div>

    <div class="footer">
        <p><strong>Miłego korzystania z systemu!</strong></p>
        <p>Zespół Hotel Booking System</p>
    </div>
</div>
</body>
</html>