# Who am I
my alias on Github is powerpoint45 but my alias on steemit is [immawake](https://steemit.com/@immawake)


![](https://i.imgur.com/rWNRw1X.png)
# Yes! A dtube Android app
This is the first release of the Dtube unofficial app! I am confident that every dtube user will love this! 

![](https://i.imgur.com/cLn2y4e.png)


# Features
* Sync app with your Steemit account
  * Subscriptions sync
  * Subscribers count sync
  * Subscription feed sync
  * Comment/reply to comments/like/dislike/subscribe using your account
* Secure login
  * This app stores your password securely using **AndroidKeyStore** and **RSA** encryption
* Privacy
   * This app is completely free from tracking of any sort!
* Search Steemit database for videos using the **asksteem** API
* A beautiful & fast UI
* Feeds
  * Subscription feed
  * Hot videos feed
  * Trending videos feed
  * New videos feed
  * Watch again feed (with removable history)
* Over the air updates to app using **github** repo
* it's a small 3mb app
* Much more and much more to come as development continues

# How do I get it?
The app can be downloaded as an **APK** (Android Package). This is a standard application package format than many of you already know about. I will eventually be putting this app on places like XDA Labs and Aptoide. Currently you can find the app on the [Github Release Page](https://github.com/powerpoint45/dtube-mobile-unofficial/releases/)  . Just download the **app-release.apk** to your phone. Next make sure you have **Unknown Sources** enabled on your phone. Go to your phone settings **>** Lock Screen and Security (or just Security depending on your phone firmware) **>** Check the box that says **Unknown Sources**. This will allow you to install apps outside the Play Store. Now you can press on the downloaded app in your status bar and it should prompt you to install the app. Let me know if you have any problems and I can help. Press the install button and the app will be installed in a couple moments. Now the app should show up in your app drawer as D.tube.

# Getting Started
Now that you got the app installed let's explore! When you open the app, you will be shown a screen like this presenting you with the hot videos feed

![](https://i.imgur.com/KRMK6tF.png)

Notice that if you click on your subscription feed or the recently watched tab that it will be completely blank. This is because you are not logged in and you have not watched any videos yet. I would start by logging in. This will unlock many of the features that makes Steemit & Dtube awesome. Press the account icon in the to right of the screen and you will be prompted to a login

![](https://i.imgur.com/6XYlckp.png)

Now enter your username and your Steemit posting key. To get your posting key, log into steemit, click your profile icon in the top right, choose "change password", select "permissions tab", press "show private key" besides the posting key, copy that key to your phone. What I do is store a file on my phone with that private key so I can go back to it anytime. You could also email it to yourself. Then just paste that private key into the posting password box and press login. As discussed earlier, your password is safely stored and encrypted inside the app. 

After logged in, your subscription feed will begin to populate and you will have your subscriptions in the right sidebar of the app as shown below

![](https://i.imgur.com/pGJgbPY.png)

Now I just encourage you to explore the app! Play some videos, comment on a video, subscribe to a channel, and like videos. I am sure you will love this app!

# Screenshots
![](https://i.imgur.com/8sOqEA4.png)

# Known Issues
There are some things I am still trying to work out completely. One is the formatting of video descriptions and comments. I'm using a very poor parsing method for this as found [here](https://github.com/powerpoint45/dtube-mobile-unofficial/blob/master/app/src/main/java/com/powerpoint45/dtube/Tools.java) in the method **getFormattedText**. Basically it needs to format it into normal HTML but it doesn't do very well as links are often fragmented and markdown is not properly processed. This is something I will have fixed soon in upcoming releases. Apart from that issue, one other one is that the app will freeze upon launch very rarely. I am not aware of what is causing this at the moment. Consider this release a beta build. More features and fixes are soon to come!

# Libraries used
I did not reinvent wheels. I am very appreciative of all those devs who make this work!
   * [com.github.curioustechizen.android-ago:library:1.3.4](https://github.com/curioustechizen/android-ago)
   * [com.squareup.picasso:picasso:2.5.2](https://github.com/square/picasso)
   * [com.github.javiersantos:AppUpdater:2.6.3](https://github.com/javiersantos/AppUpdater)
   * [com.makeramen:roundedimageview:2.3.0](https://github.com/vinc3m1/RoundedImageView)
   * [com.android.support:appcompat-v7:25.3.1](https://developer.android.com/topic/libraries/support-library/packages.html)
   * [com.android.support:design:25.3.1](https://developer.android.com/topic/libraries/support-library/packages.html)
   * [com.android.support:recyclerview-v7:25.3.1](https://developer.android.com/topic/libraries/support-library/packages.html)
   * [Steemit JS API ](https://github.com/steemit/steem-js)(locally stored on app)
   * [dtube embedded player](https://github.com/dtube/embed) (locally stored on app)
   * [Asksteem API](https://steemit.com/steemit/@thekyle/introducing-asksteem-a-steem-search-engine)

# Download
Just in case you missed the download link mentioned in the article. 
[Download Here](https://github.com/powerpoint45/dtube-mobile-unofficial/releases/)

<br /><hr/><em>Posted on <a href="https://utopian.io/utopian-io/@immawake/introducing-the-dtube-mobile-app-unofficial-android-app">Utopian.io -  Rewarding Open Source Contributors</a></em><hr/>
