<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Przypomnienie o przyjeździe</title>
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
            border-bottom: 3px solid #ffc107;
            padding-bottom: 20px;
            margin-bottom: 30px;
        }
        .header h1 {
            color: #ffc107;
            margin: 0;
            font-size: 28px;
        }
        .reminder-badge {
            background: #ffc107;
            color: #212529;
            padding: 10px 20px;
            border-radius: 25px;
            display: inline-block;
            margin: 15px 0;
            font-weight: bold;
        }
        .countdown {
            background: #fff3cd;
            border: 2px solid #ffc107;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
            margin: 20px 0;
        }
        .countdown .days {
            font-size: 48px;
            font-weight: bold;
            color: #856404;
        }
        .info-box {
            background: #d1ecf1;
            border: 1px solid #bee5eb;
            color: #0c5460;
            padding: 15px;
            border-radius: 8px;
            margin: 20px 0;
        }
        .checklist {
            background: #d4edda;
            border: 1px solid #c3e6cb;
            padding: 15px;
            border-radius: 8px;
            margin: 20px 0;
        }
        .checklist ul {
            margin: 0;
            padding-left: 20px;
        }
        .checklist li {
            margin: 8px 0;
            color: #155724;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>🏨 Przypomnienie o przyjeździe</h1>
        <div class="reminder-badge">Zbliża się Twój wyjazd!</div>
    </div>

    <p>Szanowny/a <strong>${userName}</strong>,</p>

    <p>Przypominamy o zbliżającym się check-in w hotelu <strong>${hotel.name}</strong>!</p>

    <div class="countdown">
        <div class="days">${daysUntilCheckIn}</div>
        <div>dni do przyjazdu</div>
    </div>

    <div class="info-box">
        <h4>📋 Szczegóły rezerwacji</h4>
        <p><strong>Hotel:</strong> ${hotel.name}</p>
        <p><strong>Pokój:</strong> ${room.roomType} (Nr ${room.roomNumber})</p>
        <p><strong>Check-in:</strong> ${formattedCheckIn} od 14:00</p>
    </div>

    <div class="checklist">
        <h4>✅ Przygotuj się do wyjazdu:</h4>
        <ul>
            <li>📄 Dowód osobisty</li>
            <li>💳 Karta płatnicza</li>
            <li>📧 Potwierdzenie rezerwacji (ten email)</li>
            <li>🧳 Spakuj bagaż</li>
            <li>🚗 Sprawdź dojazd</li>
        </ul>
    </div>

    <div class="info-box">
        <h4>📍 Informacje kontaktowe:</h4>
        <p><strong>Adres:</strong> ${hotel.location}</p>
        <p><strong>Telefon:</strong> +48 123 456 789</p>
    </div>

    <div class="footer">
        <p><strong>Nie możemy się doczekać Twojego przyjazdu!</strong></p>
        <p>Zespół ${hotel.name}</p>
    </div>
</div>
</body>
</html>