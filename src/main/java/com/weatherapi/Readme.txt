
*********** API TEST OTOMASYONU ***********

Bu görevde, OpenWeather API’yi kullanarak test senaryolarını çalıştıran
bir API test otomasyonu geliştirmesi yaptım.

Test edilecek API: https://openweathermap.org/current
Api Key: e862540ab1126a114cd3a914bfba7abf
Herhangi bir test aracini kullanabilirsiniz.

Çalıştırılması gereken test senaryoları:
-Başarılı şehir sorgusu → (200 OK yanıtı alınmalı)
-Hatalı şehir sorgusu → (404 Not Found hatası alınmalı)
-API Key olmadan çağrı yapma → (401 Unauthorized hatası alınmalı)
-Yanıt formatının doğrulanması → (main.temp, weather.description gibi
alanların beklenen formatta olması)