package com.sap.inspection.connection;
//package com.domikado.marketplace.connection;
//
//import android.content.Context;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.domikado.marketplace.constant.Constants;
//import com.sap.inspection.R;
//
///**
// * An asynchronous task that communicates with Google to 
// * retrieve a request token.
// * (OAuthGetRequestToken)
// * 
// * After receiving the request token from Google, 
// * show a browser to the user to authorize the Request Token.
// * (OAuthAuthorizeToken)
// * 
// */
//public class OAuthRequestTokenTask extends AsyncTask<Void, Void, Void> {
//
//	final String TAG = getClass().getName();
//	private Context	context;
//	private OAuthProvider provider;
//	private OAuthConsumer consumer;
//	private final String oauthTokenParam = "oauth_token=";
//	private String[] credential;
//	private Handler handler;
//
//	public OAuthRequestTokenTask(Context context, String username, String password, Handler handler) {
//		this.context = context;
//		this.credential = new String[2];
//		this.credential[0] = username;
//		this.credential[1] = password;
//		this.handler = handler;
////		credential[0] = "user_1@example.com";
////		credential[1] = "123456";
//	}
//
//	/**
//	 * 
//	 * Retrieve the OAuth Request Token and present a browser to the user to authorize the token.
//	 * 
//	 */
//	@Override
//	protected Void doInBackground(Void... params) {
//		try{
//			this.consumer = new CommonsHttpOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
//			this.provider = new CommonsHttpOAuthProvider(
//					Constants.REQUEST_URL,
//					Constants.ACCESS_URL,
//					Constants.AUTHORIZE_URL);
//
//			MyXAuth auth = new MyXAuth(consumer,provider,Constants.OAUTH_CALLBACK_URL);
//			String authUrl = auth.getRequestToken();
//			Log.d("here", "return url : "+authUrl);
//			int idx = authUrl.indexOf(oauthTokenParam);
//			String verifier = authUrl.substring(idx+oauthTokenParam.length());
//			Log.d("here", "verifier : "+verifier);
//			String[] string = auth.getAccessToken(credential);
//			for (int i = 0; i < string.length; i++) {
//				Log.e(getClass().getName(), "return access "+i+" : "+string[i]);
//			}
//			
//			// TODO pindahin ini keluar dari asyncTask
//			if (!string[0].equalsIgnoreCase("unauthorized")){
//				TokenModel model = new TokenModel();
//				model.accToken = string[0];
//				Log.e(getClass().getName(), "token wanted to save");
//				model.save(context);
//			}else {
//				Looper.prepare();
//				Toast.makeText(context,R.string.failed_login_unauthorized ,Toast.LENGTH_LONG);
//			}
//		} catch (Exception e) {
//			Log.e(TAG, "Error during OAUth retrieve request token", e);
//		}
//
//		return null;
//	}
//	
//	@Override
//	protected void onPostExecute(Void arg0) {
//		super.onPostExecute(arg0);
//		handler.sendEmptyMessage(0);
//	}
//}