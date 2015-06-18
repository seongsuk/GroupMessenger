package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Date;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    Message toSend;
    int seq = 0;
    String myPort;
    mySQLite myDB;
    Vector<Message> Store;
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    //static Vector<String> REMOTE_PORTS;
    static Vector<String> TEST_PORTS;
    static final String REMOTE_PORTS[] = {"11108", "11112", "11116", "11120", "11124"};
    static final int SERVER_PORT = 10000;
    static  String REMOTE_PORT0 = "11108";
    int ordered = 0;
    TreeMap<Timestamp,String> msgMap;
    Vector<String> dupCheck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        myDB = new mySQLite(this);
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        Button send = (Button) findViewById(R.id.button4);
        final EditText editText = (EditText) findViewById(R.id.editText1);
        final TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        Store = new Vector<Message>();
        Uri.Builder newUri = new Uri.Builder();
        newUri.authority("content");
        newUri.scheme("edu.buffalo.cse.cse486586.groupmessenger2.provider");
        newUri.build();
//        REMOTE_PORTS = new Vector<String>();
//        REMOTE_PORTS.add("11108");
//        REMOTE_PORTS.add("11112");
//        REMOTE_PORTS.add("11116");
//        REMOTE_PORTS.add("11120");
//        REMOTE_PORTS.add("11124");

        TEST_PORTS = new Vector<String>();
        TEST_PORTS.add("11108");
        TEST_PORTS.add("11112");
        //Referenced http://stackoverflow.com/questions/12947088/java-treemap-comparator
        msgMap = new TreeMap<Timestamp,String>(new Comparator<Timestamp>() {
            @Override
            public int compare(Timestamp t1, Timestamp t2) {
                if(t1.compareTo(t2)!=0)
                    return t1.compareTo(t2);
                else return 1;
            }});
        dupCheck = new Vector<String>();

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString() + "\n";
                tv.append(msg);
                //toSend = new Message(myPort,msg);
                editText.setText(""); // This is one way to reset the input box.
                Date currentDate= new Date();
                Timestamp time = new Timestamp(currentDate.getTime());
                //Message temp = new Message(myPort, msgs[0], time);////
                String times = ""+time;
                Sender0 sender0 = new Sender0(msg,times); //Sender for port 11108
                Sender1 sender1 = new Sender1(msg,times);//Sender for port 11112
                Sender2 sender2 = new Sender2(msg,times);//Sender for port 11116
                Sender3 sender3 = new Sender3(msg,times);//Sender for port 11120
                Sender4 sender4 = new Sender4(msg,times);//Sender for port 11124
//                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets){
            ServerSocket serverSocket = sockets[0];
            Socket socket = new Socket();
            try {
                while(true) {
                    socket = serverSocket.accept();
                    InputStream inputstream = socket.getInputStream();
                    DataInputStream in = new DataInputStream(new BufferedInputStream(inputstream));
//                    Message text = (Message) in.readObject();
                    String msg = ""+in.readUTF();
                    String time = ""+in.readUTF();

//                    String temp = text.getTime() + "||" + text.getText();
                    String temp = time+"||"+msg;
                    Log.d("RAW_MESSAGE","Message Received: "+temp);
                    publishProgress(temp);
                    in.close();
                    // Store.add(text);

                    //Socket nSocket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    //          Integer.parseInt(REMOTE_PORTS[i]));
                    // OutputStream asd = nSocket.getOutputStream();
                    //   ObjectOutputStream sendOut = new ObjectOutputStream(asd);
                    //    sendOut.writeObject(text);
                    //    sendOut.flush();
                    //    sendOut.close();
                    ////socket.close();
                    ////  serverSocket.close();


//                    if (socket.isInputShutdown()) return null;

                }} catch (IOException e) {
                e.printStackTrace();
            } finally{
                try {
                    socket.close();
                    serverSocket.close();////
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return null;
        }
//        private boolean isRemotePortInUse(int portNumber) {
//            try {
//                // Socket try to open a REMOTE port
//                new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), portNumber).close();
//                // remote port can be opened, this is a listening port on remote machine
//                // this port is in use on the remote machine !
//                return true;
//            } catch(Exception e) {
//                // remote port is closed, nothing is running on
//                return false;
//            }
//        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            try {
                sequencer(strReceived);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */

//            String filename = "GroupMessengerOutput";
//            String string = strReceived + "\n";
//            FileOutputStream outputStream;
//
//            try {
//                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//                outputStream.write(string.getBytes());
//                outputStream.close();
//            } catch (Exception e) {
//                Log.e(TAG, "File write failed");
//            }

            return;
        }
    }
    public void sequencer(String received) throws ParseException {
        //Referenced http://stackoverflow.com/questions/18350065/convert-yyyy-mm-dd-hhmmss-sss-zzz-format-strin-to-date-object
        //http://www.101apps.co.za/articles/using-a-content-resolver-to-access-another-app-s-database.html


        // getContentResolver().delete( Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger2.provider"),"_id < 1000",null);
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        String rec[] = received.split("\\|\\|");
        String time = rec[0];
        String msgToSend = rec[1];
        //String time = received.split("\\|\\|")[0];
        //String msgToSend = received.split("\\|\\|")[1];
        Date newDate = date.parse(time);
        Timestamp timestamp = new Timestamp(newDate.getTime());

        msgMap.put(timestamp,msgToSend);
        int seq = 0;
        String key;


        for(String value : msgMap.values()){
//                if(ordered==10) {
            key = ""+seq++;
            ContentValues keyValueToInsert = new ContentValues();
            keyValueToInsert.put("key", "" + key);
            keyValueToInsert.put("value", value);


            Uri newUri = getContentResolver().insert(
                    Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger2.provider"),    // assume we already created a Uri object with our provider URI
                    keyValueToInsert
            );
//                }

        }
//        keyValueToInsert.put("key", "" + text.getSeq());
//        keyValueToInsert.put("value", text.getText());
    }
    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */


    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

//            Date currentDate= new Date();
//            Timestamp time = new Timestamp(currentDate.getTime());
//            //Message temp = new Message(myPort, msgs[0], time);////
//            String times = ""+time;
//            Sender0 sender0 = new Sender0(msgs[0],times);
//            //Sender1 sender1 = new Sender1(msgs[0],times);
            //Sender2 sender2 = new Sender2(msgs[0],times);
            //Sender3 sender3 = new Sender3(msgs[0],times);
            //Sender4 sender4 = new Sender4(msgs[0],times);
//            try {
////                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
////                            Integer.parseInt(REMOTE_PORT0));
//                for(int i = 0; i <= 2; i++) {
//                      Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
//                      Integer.parseInt(REMOTE_PORTS[i]));
//                    socket.setSoTimeout(500);
////                    Socket socket = new Socket();
////                    socket.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
////                            Integer.parseInt(REMOTE_PORTS[i])), 500);
//                    socket.setTcpNoDelay(true);
//                    OutputStream outputStream = socket.getOutputStream();
//                    DataOutputStream o = new DataOutputStream(new BufferedOutputStream(outputStream));
////                    o.writeUTF(myPort);
//                    o.writeUTF(msgs[0]);
//                    o.writeUTF(""+time);
//                    o.flush();////
//                    socket.close();
//                }
//            }
//            catch (UnknownHostException e) {
//                Log.e(TAG, "ClientTask UnknownHostException");
//            } catch (IOException e) {
//                Log.e(TAG, "ClientTask socket IOException");
//            }

            return null;
        }
//        socket.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
//                Integer.parseInt(i)) , 1390);
//        socket.setTcpNoDelay(true);
    }
    public class Sender0 extends Thread {
        String msg;
        String time;
        public Sender0(String msg, String time) {
            this.msg = msg;
            this.time = time;
            start();
        }

        public void run() {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        11108);
                socket.setSoTimeout(330);
                socket.setTcpNoDelay(true);
                    DataOutputStream dos =
                            new DataOutputStream(
                                    new BufferedOutputStream( socket.getOutputStream() ));
                    dos.writeUTF(msg);
                    dos.writeUTF(time);
                    dos.flush();
                    socket.close();

            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
    public class Sender1 extends Thread {
        String msg;
        String time;
        public Sender1(String msg, String time) {
            this.msg = msg;
            this.time = time;
            start();
        }

        public void run() {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        11112);
                socket.setSoTimeout(330);
                socket.setTcpNoDelay(true);
                    DataOutputStream dos =
                            new DataOutputStream(
                                    new BufferedOutputStream( socket.getOutputStream() ));
                    dos.writeUTF(msg);
                    dos.writeUTF(time);
                    dos.flush();
                    socket.close();

            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
    public class Sender2 extends Thread {
        String msg;
        String time;
        public Sender2(String msg, String time) {
            this.msg = msg;
            this.time = time;
            start();
        }

        public void run() {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        11116);
               socket.setSoTimeout(330);
                socket.setTcpNoDelay(true);
                    DataOutputStream dos =
                            new DataOutputStream(
                                    new BufferedOutputStream( socket.getOutputStream() ));
                    dos.writeUTF(msg);
                    dos.writeUTF(time);
                    dos.flush();
                    socket.close();

            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
    public class Sender3 extends Thread {
        String msg;
        String time;
        public Sender3(String msg, String time) {
            this.msg = msg;
            this.time = time;
            start();
        }

        public void run() {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        11120);
             socket.setSoTimeout(330);
                socket.setTcpNoDelay(true);
                    DataOutputStream dos =
                            new DataOutputStream(
                                    new BufferedOutputStream( socket.getOutputStream() ));
                    dos.writeUTF(msg);
                    dos.writeUTF(time);
                    dos.flush();
                    socket.close();

            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
    public class Sender4 extends Thread {
        String msg;
        String time;
        public Sender4(String msg, String time) {
            this.msg = msg;
            this.time = time;
            start();
        }

        public void run() {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        11124);
              socket.setSoTimeout(330);
                socket.setTcpNoDelay(true);
                    DataOutputStream dos =
                            new DataOutputStream(
                                    new BufferedOutputStream( socket.getOutputStream() ));
                    dos.writeUTF(msg);
                    dos.writeUTF(time);
                    dos.flush();
                    socket.close();
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDB.close();
    }
}
