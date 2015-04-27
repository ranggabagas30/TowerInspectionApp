package com.sap.inspection.connection;
//package com.domikado.marketplace.connection;
//
//import java.io.IOException;
//
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.io.UnsupportedEncodingException;
//import java.security.NoSuchAlgorithmException;
//import java.util.Arrays;
//import java.util.List;
//
//import oauth.signpost.OAuth;
//import oauth.signpost.OAuthConsumer;
//import oauth.signpost.OAuthProvider;
//import oauth.signpost.exception.OAuthCommunicationException;
//import oauth.signpost.exception.OAuthExpectationFailedException;
//import oauth.signpost.exception.OAuthMessageSignerException;
//import oauth.signpost.exception.OAuthNotAuthorizedException;
//import oauth.signpost.http.HttpRequest;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.protocol.HTTP;
//
//
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.domikado.marketplace.constant.Constants;
//import com.rindang.tools.SHA1;
//
//public class MyXAuth {
//
//    private static final String TAG = "OAuthHelper";
//
//    private OAuthConsumer mConsumer;
//    private OAuthProvider mProvider;
//
//    private String mCallbackUrl;
//
//    public MyXAuth(OAuthConsumer consumer, OAuthProvider provider, String callbackUrl)
//    throws UnsupportedEncodingException {
//
//        mConsumer = consumer;
//        mProvider = provider;
//        mProvider.setOAuth10a(true);
//
//        mCallbackUrl = (callbackUrl == null ? OAuth.OUT_OF_BAND : callbackUrl);
//    }
//
//    public String getRequestToken()
//    throws OAuthMessageSignerException, OAuthNotAuthorizedException,
//    OAuthExpectationFailedException, OAuthCommunicationException {
//        String authUrl =
//                mProvider.retrieveRequestToken(mConsumer, mCallbackUrl);
//        return authUrl;
//    }
//
////    public String[] getAccessToken(String verifier)
////    throws OAuthMessageSignerException, OAuthNotAuthorizedException,
////    OAuthExpectationFailedException, OAuthCommunicationException {
////        mProvider.retrieveAccessToken(mConsumer, verifier);
////        return new String[] {
////                mConsumer.getToken(), mConsumer.getTokenSecret()
////        };
////    }
////
////    public String[] getToken() {
////        return new String[] {
////                mConsumer.getToken(), mConsumer.getTokenSecret()
////        };
////    }
////
////    public void setToken(String token, String secret) {
////        mConsumer.setTokenWithSecret(token, secret);
////    }
////
////    public String getUrlContent(String url)
////    throws OAuthMessageSignerException, OAuthExpectationFailedException,
////    OAuthCommunicationException, IOException {
////        HttpGet request = new HttpGet(url);
////
////        // sign the request
////        mConsumer.sign(request);
////
////        // send the request
////        HttpClient httpClient = new DefaultHttpClient();
////        HttpResponse response = httpClient.execute(request);
////
////        // get content
////        BufferedReader in = new BufferedReader(
////                new InputStreamReader(response.getEntity().getContent()));
////        StringBuffer sb = new StringBuffer("");
////        String line = "";
////        String NL = System.getProperty("line.separator");
////        while ((line = in.readLine()) != null)
////            sb.append(line + NL);
////        in.close();
////
////        return sb.toString();
////    }
////
////    public String buildXOAuth(String email) {
////        String url =
////            String.format("https://mail.google.com/mail/b/%s/smtp/", email);
////        HttpRequestAdapter request = new HttpRequestAdapter(new HttpGet(url));
////
////        // Sign the request, the consumer will add any missing parameters
////        try {
////            mConsumer.sign(request);
////        } catch (OAuthMessageSignerException e) {
////            Log.e(TAG, "failed to sign xoauth http request " + e);
////            return null;
////        } catch (OAuthExpectationFailedException e) {
////            Log.e(TAG, "failed to sign xoauth http request " + e);
////            return null;
////        } catch (OAuthCommunicationException e) {
////            Log.e(TAG, "failed to sign xoauth http request " + e);
////            return null;
////        }
////        HttpParameters params = mConsumer.getRequestParameters();
////
////        // Since signpost doesn't put the signature into params,
////        // we've got to create it again.
////        OAuthMessageSigner signer = new HmacSha1MessageSigner();
////        signer.setConsumerSecret(mConsumer.getConsumerSecret());
////        signer.setTokenSecret(mConsumer.getTokenSecret());
////        String signature;
////        try {
////            signature = signer.sign(request, params);
////        } catch (OAuthMessageSignerException e) {
////            Log.e(TAG, "invalid oauth request or parameters " + e);
////            return null;
////        }
////        params.put(OAuth.OAUTH_SIGNATURE, OAuth.percentEncode(signature));
////
////        StringBuilder sb = new StringBuilder();
////        sb.append("GET ");
////        sb.append(url);
////        sb.append(" ");
////        int i = 0;
////        for (Entry<String, SortedSet<String>> entry : params.entrySet()) {
////            String key = entry.getKey();
////            String value = entry.getValue().first();
////
////            int size = entry.getValue().size();
////            if (size != 1)
////                Log.d(TAG, "warning: " + key + " has " + size + " values");
////
////            if (i++ != 0)
////                sb.append(",");
////            sb.append(key);
////            sb.append("=\"");
////            sb.append(value);
////            sb.append("\"");
////        }
////        Log.d(TAG, "xoauth encoding " + sb);
////
//////        Base64 base64 = new Base64();
////        try {
////            byte[] buf = Base64.encode(sb.toString().getBytes("utf-8"),Base64.DEFAULT);
////            return new String(buf, "utf-8");
////        } catch (UnsupportedEncodingException e) {
////            Log.e(TAG, "invalid string " + sb);
////        }
////
////        return null;
////    }
//    
//    public String[] getAccessToken(String[] credentials) {
//		HttpClient client = new DefaultHttpClient();
//		HttpPost request = new HttpPost(Constants.ACCESS_URL);
//		List<BasicNameValuePair> params = Arrays.asList(
//				new BasicNameValuePair("x_auth_login", credentials[0]),
//				new BasicNameValuePair("x_auth_password", credentials[1]),
//				new BasicNameValuePair("x_auth_mode", "mobile_auth"));
//		UrlEncodedFormEntity entity = null;
//		try {
////			request.addHeader("DOMIKADO-SIGNATURE", "09ab1a2cb483240a91601a2394aec8d7113ec966");
////			request.addHeader("DOMIKADO-APP-ID", "nMsDEkH5eytltNKq0wbxJia2Tga0e5xk84dlKqQJ");
//			Log.e("here", "is sha1 eequal : "+(SHA1.SHA1(Constants.CONSUMER_KEY+Constants.CONSUMER_SECRET).equalsIgnoreCase("09ab1a2cb483240a91601a2394aec8d7113ec966")));
//		request.addHeader("DOMIKADO-SIGNATURE", SHA1.SHA1(Constants.CONSUMER_KEY+Constants.CONSUMER_SECRET));
//		request.addHeader("DOMIKADO-APP-ID", Constants.CONSUMER_KEY);
//			entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
//		} catch (UnsupportedEncodingException e) {
//			throw new RuntimeException("wtf");
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		}
//		request.setEntity(entity);
//		try {
//			HttpRequest signedRequest = mConsumer.sign(request);
//		} catch (OAuthMessageSignerException e) {
//			e.printStackTrace();
//		} catch (OAuthExpectationFailedException e) {
//			e.printStackTrace();
//		} catch (OAuthCommunicationException e) {
//			e.printStackTrace();
//		}
//		HttpResponse response;
//		InputStream data = null;
//		try {
//			response = client.execute(request);
//			data = response.getEntity().getContent();
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		String responseString = null;
//	    try
//	    {
//	      final char[] buffer = new char[0x10000];
//	      StringBuilder out = new StringBuilder();
//	      Reader in = new InputStreamReader(data, HTTP.UTF_8);
//	      int read;
//	      do
//	      {
//	        read = in.read(buffer, 0, buffer.length);
//	        if (read > 0)
//	        {
//	          out.append(buffer, 0, read);
//	        }
//	      } while (read >= 0);
//	      in.close();
//	      responseString = out.toString();
//	    } catch (IOException ioe)
//	    {
//	      throw new IllegalStateException("Error while reading response body", ioe);
//	    }
//	    
//	    return TextUtils.split(responseString, "&");
//	}
//
//}
