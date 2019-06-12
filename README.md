# TKU課表爬蟲APP
此為Android Studio團隊作業
  
### 使用工具
* Jsoup
* 正規表示法


### 專案架構
* `WWW.java` -- 用來進行Request及取得Response

* `AESCrypt.java` -- 用於進行AES加密解密的動作

* `MainActivity.java` -- 開啟App的首個頁面,其保存了使用者的訊息,
                         會按照使用者的行為選擇傳遞資料到`LoginActivity`或是`TimeTableActivity`,
                         並提供了一些對外開放存取資料的函數
  * `MainActivityAdapter.java` -- 負責控管主頁面的使用者清單
  
* `LoginActivity.java` -- 負責處理使用者的登入動作,
                          以及處理登入後的回傳的資訊

* `TimeTableActivity.java` -- 使用從LoginActivity傳入的資料建構課表
