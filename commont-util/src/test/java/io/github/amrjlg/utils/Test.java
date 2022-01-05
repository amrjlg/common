/*
 * Copyright (c) 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.github.amrjlg.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Test {
    public static void main(String[] args) throws Exception {


        String api = "https://oapi.dingtalk.com/robot/send?access_token=e2cb495767463fd1e918f6036239de2b523c497926b0732463b168e5d91147d8";

        HttpURLConnection post = connection(api, "POST", 50000, 50000);
        post.setRequestProperty("Content-Type","application/json");

        OutputStream outputStream = post.getOutputStream();
        outputStream.write("{\"msgtype\":\"text\",\"text\":{\"content\":\"@17782266729 17782266729,我爱你\"},\"at\":{\"atUserIds\":17782266729}}".getBytes(StandardCharsets.UTF_8));
        outputStream.flush();

        InputStream inputStream = post.getInputStream();
        int available = inputStream.available();
        byte[] bytes = new byte[available];
        inputStream.read(bytes);
        System.out.println(new String(bytes,StandardCharsets.UTF_8));


    }


    public static HttpURLConnection connection(String api, String method, int connectTimmOut, int readTimeOut) throws IOException {
        URL url = new URL(api);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(connectTimmOut);
        conn.setReadTimeout(readTimeOut);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod(method);
        conn.setRequestProperty("Connection", "Keep-Alive");
        return conn;
    }

}