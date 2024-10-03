# READ: DEPRECATED

This project is now being archived by me. Disregard the README below it.

I made this project as part of my requirement to fulfill a school project by me and my teacher. However, the project got cancelled due to unknown reasons, and I was forced to scrap it after months of hard work.

This wasn't the end though. I still had the passion to continue this even without support from the school, just for the sake of making my very own application with some sense of function and quality. With this, I decided to archive this project and start again from scratch, because that was the only way I could ever get this done without parsing through files I barely remember anymore.

Again, this project is being archived.

# Tardy Scan - Android Port

This project is currently in development, to be used (currently) at Pasig City Science High School.

This is an app that simply adds student records to a database and records their tardiness using an encrypted QR code to minimize the risk of posing as another student. When the QR is scanned, the information inside is immediately added to the database.

## But, why do this?

There are a few reasons behind creating a project like this. Here are the top reasons:

* I want to include this in my college apps.
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
