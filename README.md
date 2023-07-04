# Tardy Scan - Android Port

This project is currently in development, to be used (currently) at Pasig City Science High School.

This is an app that simply adds student records to a database and records their tardiness using an encrypted QR code to minimize the risk of posing as another student. When the QR is scanned, the information inside is immediately added to the database.

## But, why do this?

There are a few reasons behind creating a project like this. Here are the top reasons:

* I was approached by my Grade 9 Math teacher, saying that if I do this successfully, I would be exempted from written works for the rest of the SY.
* I can (hopefully) get royalties when having this implemented at other local schools.

## Todos

* ~~actually start the project loooool~~
* make a main page for the app
* add authentication

## Contributing and Config

To contribute, you can download the entire zip folder and set the following up:
* Create a new `server_link.properties` file in the `app/res/raw` folder. This file should contain the following:
```properties
SECRET_KEY=<YOUR SECRET ENCRYPTION/DECRYPTION KEY HERE>
SUPABASE_URL=<YOUR SUPABASE URL HERE>
SUPABASE_KEY=<YOUR SUPABASE PUBLIC KEY HERE>
```
