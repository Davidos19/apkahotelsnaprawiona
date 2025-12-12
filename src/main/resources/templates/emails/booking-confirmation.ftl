<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Potwierdzenie rezerwacji</title>
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
            border-bottom: 3px solid #007bff;
            padding-bottom: 20px;
            margin-bottom: 30px;
        }
        .header h1 {
            color: #007bff;
            margin: 0;
            font-size: 28px;
        }
        .success-badge {
            background: #28a745;
            color: white;
            padding: 10px 20px;
            border-radius: 25px;
            display: inline-block;
            margin: 15px 0;
            font-weight: bold;
        }
        .booking-details {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            border-left: 5px solid #007bff;
            margin: 20px 0;
        }
        .detail-row {
            display: flex;
            justify-content: space-between;
            margin: 10px 0;
            padding: 5px 0;
            border-bottom: 1px solid #eee;
        }
        .detail-label {
            font-weight: bold;
            color: #555;
        }
        .detail-value {
            color: #007bff;
            font-weight: bold;
        }
        .price-highlight {
            background: #fff3cd;
            padding: 15px;
            border-radius: 8px;
            text-align: center;
            margin: 20px 0;
            border: 2px solid #ffc107;
        }
        .price-highlight .amount {
            font-size: 24px;
            color: #856404;
            font-weight: bold;
        }
        .footer {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #eee;
            text-align: center;
            color: #666;
            font-size: 14px;
        }
        .cta-button {
            background: #007bff;
            color: white;
            padding: 12px 30px;
            border: none;
            border-radius: 25px;
            text-decoration: none;
            display: inline-block;
            margin: 20px 0;
            font-weight: bold;
        }
        .info-box {
            background: #d1ecf1;
            border: 1px solid #bee5eb;
            color: #0c5460;
            padding: 15px;
            border-radius: 8px;
            margin: 20px 0;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>🏨 Potwierdzenie Rezerwacji</h1>
        <div class="success-badge">✅ Rezerwacja potwierdzona</div>
    </div>

    <p>Szanowny/a <strong>${userName}</strong>,</p>

    <p>Z przyjemnością potwierdzamy Twoją rezerwację w hotelu <strong>${hotel.name}</strong>!</p>

    <div class="booking-details">
        <h3>📋 Szczegóły rezerwacji</h3>

        <div class="detail-row">
            <span class="detail-label">ID rezerwacji:</span>
            <span class="detail-value">#${reservation.id}</span>
        </div>

        <div class="detail-row">
            <span class="detail-label">Hotel:</span>
            <span class="detail-value">${hotel.name}</span>
        </div>

        <div class="detail-row">
            <span class="detail-label">Lokalizacja:</span>
            <span class="detail-value">${hotel.city}</span>
        </div>

        <div class="detail-row">
            <span class="detail-label">Pokój:</span>
            <span class="detail-value">${room.roomType} (Nr ${room.roomNumber})</span>
        </div>

        <div class="detail-row">
            <span class="detail-label">Pojemność:</span>
            <span class="detail-value">${room.capacity} osób</span>
        </div>

        <div class="detail-row">
            <span class="detail-label">Check-in:</span>
            <span class="detail-value">${formattedCheckIn}</span>
        </div>

        <div class="detail-row">
            <span class="detail-label">Check-out:</span>
            <span class="detail-value">${formattedCheckOut}</span>
        </div>

        <div class="detail-row">
            <span class="detail-label">Liczba nocy:</span>
            <span class="detail-value">${totalNights}</span>
        </div>
    </div>

    <div class="price-highlight">
        <div>💰 Całkowita kwota:</div>
        <div class="amount">${reservation.totalPrice} PLN</div>
    </div>

    <div class="info-box">
        <h4>📌 Ważne informacje:</h4>
        <ul>
            <li><strong>Check-in:</strong> od 14:00</li>
            <li><strong>Check-out:</strong> do 12:00</li>
            <li>Przy zameldowaniu prosimy o dokument tożsamości</li>
            <li>W razie pytań, skontaktuj się z recepcją</li>
        </ul>
    </div>

    <div style="text-align: center;">
        <a href="#" class="cta-button">Zarządzaj rezerwacją</a>
    </div>

    <div class="footer">
        <p><strong>Dziękujemy za wybór naszego hotelu!</strong></p>
        <p>Zespół ${hotel.name}</p>
        <hr>
        <p style="font-size: 12px; color: #888;">
            Ta wiadomość została wygenerowana automatycznie. Prosimy nie odpowiadać na ten email.
        </p>
    </div>
</div>
</body>
</html>