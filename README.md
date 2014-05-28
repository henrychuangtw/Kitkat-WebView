Kitkat-WebView
==============

A solution for Android 4.4 (KitKat) webiew issue :

1. openFileChooser not called when <input type="file"> is clicked on android 4.4 webview
   https://code.google.com/p/android/issues/detail?id=62220

2. Android 4.4 WebView cannot load "content://" urls in html page
   https://code.google.com/p/android/issues/detail?id=63033


Note : https://developer.chrome.com/multidevice/webview/overview

If you are currently using content:// URLs to load files from a content provider in your application, note that these URLs only work when accessed from local content. That is, web content hosted outside your application is not allowed to access files built into your application. 



Solution :

1. Add an  input type="button"  in web page, 
   when click the button, use javascript call app function to choose.

2. Convert choosed images to base64 string and pass it to web page.

3. Show image : img.src="data:image/png;base64,xxxxx  base64 string form app  xxxxx"> 

4. Post the base64 string to your server and decode it, then your can save image.


 
This is my solution for Android 4.4(Kitkat) webview, 
if you have other solution, please tell me, thx.

henrychuang.tw@gmal.com
