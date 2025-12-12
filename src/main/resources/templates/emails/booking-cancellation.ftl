<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Anulowanie rezerwacji</title>
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
            border-bottom: 3px solid #dc3545;
            padding-bottom: 20px;
            margin-bottom: 30px;
        }
        .header h1 {
            color: #dc3545;
            margin: 0;
            font-size: 28px;
        }
        .cancellation-badge {
            background: #dc3545;
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
            border-left: 5px solid #dc3545;
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
            color: #dc3545;
            font-weight: bold;
        }
        .info-box {
            background: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
            padding: 15px;
            border-radius: 8px;
            margin: 20px 0;
        }
        .footer {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #eee;
            text-align: center;
            color: #666;
            font-size: 14px;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>❌ Anulowanie Rezerwacji</h1>
        <div class="cancellation-badge">Rezerwacja anulowana</div>
    </div>

    <p>Szanowny/a <strong>${userName}</strong>,</p>

    <p>Potwierdzamy anulowanie Twojej rezerwacji w hotelu <strong>${hotel.name}</strong>.</p>

    <div class="booking-details">
        <h3>📋 Szczegóły anulowanej rezerwacji</h3>

        <div class="detail-row">
            <span class="detail-label">ID rezerwacji:</span>
            <span class="detail-value">#${reservation.id}</span>
        </div>

        <div class="detail-row">
            <span class="detail-label">Hotel:</span>
            <span class="detail-value">${hotel.name}</span>
        </div>

        <div class="detail-row">
            <span class="detail-label">Check-in:</span>
            <span class="detail-value">${formattedCheckIn}</span>
        </div>

        <div class="detail-row">
            <span class="detail-label">Check-out:</span>
            <span class="detail-value">${formattedCheckOut}</span>
        </div>
    </div>

    <div class="info-box">
        <h4>💰 Zwrot płatności</h4>
        <p>Jeśli dokonałeś płatności, środki zostaną zwrócone na Twoje konto w ciągu 3-5 dni roboczych.</p>
    </div>

    <div class="footer">
        <p><strong>Dziękujemy za skorzystanie z naszych usług</strong></p>
        <p>Zespół ${hotel.name}</p>
    </div>
</div>
</body>
</html>