//package ga.jundbits.dareme;
//
//import android.app.Activity;
//import android.os.AsyncTask;
//import android.util.Log;
//
//@SuppressWarnings("rawtypes")
//public class SendMailTask extends AsyncTask {
//
//    private Activity sendMailActivity;
//
//    public SendMailTask(Activity activity) {
//        sendMailActivity = activity;
//
//    }
//
//    protected void onPreExecute() {
//
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    protected Object doInBackground(Object... args) {
//        try {
//            Log.i("SendMailTask", "About to instantiate GMail...");
//            publishProgress("Processing input....");
//            GMail androidEmail = new GMail(args[0].toString(),
//                    args[1].toString(),  args[2].toString(), args[3].toString(),
//                    args[4].toString());
//            publishProgress("Preparing mail message....");
//            androidEmail.createEmailMessage();
//            publishProgress("Sending email....");
//            androidEmail.sendEmail();
//            publishProgress("Email Sent.");
//            Log.i("SendMailTask", "Mail Sent.");
//
//
//        } catch (Exception e) {
//            publishProgress(e.getMessage());
//            Log.e("SendMailTask", e.getMessage(), e);
//        }
//        return null;
//    }
//
//    @Override
//    public void onProgressUpdate(Object... values) {
//
//    }
//
//    @Override
//    public void onPostExecute(Object result) {
//
//    }
//
//
//}
