# displateartchecker
An app to check for stolen art on Displate.

You need the following to run DisplateChecker:

- Java, if you don't have it already (https://java.com/en/download/)
- Google Chrome, if you don't have it already (https://www.google.com/intl/en_uk/chrome/)
- Chromedriver (https://chromedriver.chromium.org/)

Place chromedriver.exe in the DisplateChecker folder. The application will not run without it.

Copy any artwork files you want to test for Displate entries into the "Test Images" folder, and delete the example image. They don't have to be high resolution; ordinary web preview size is fine.

Open searchterms.txt, and enter the terms you want to search Displate for (the more you add, the longer it will take to complete - it staggers requests to prevent putting undue pressure on Displate's servers). For example, if your artwork contains art of Tifa, Aerith and Mai Shiranui, put those into the text file (one entry per line). You can delete the example lines if you like.

This application makes use of Selenium, which will open up a Chrome browser window on its own and automate browsing Displate when you press the "Retrieve Art from Displate" button.
Don't minimise, resize, or close the window; you can still do other things while it's working, or keep other windows on top of it. It will close itself once it's finished.
