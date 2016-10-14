package cn.fcrj.pickphoto;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketService {

    private int _PORT = 9999;
    private int _UPLOAD = 8888;//状态
    private String _ADDRESS = "192.168.2.1";
    String tag = "SocketService=====";
    File file=null;
    String sendinfo=null;
    //socket执行结果
    public int status;

    public SocketService(File file, String sendinfo, String address, int port, int upload) {
        super();
        this.file = file;
        this.sendinfo = sendinfo;
        this._ADDRESS = address;
        this._PORT = port;
        this._UPLOAD = upload;
    }


    /**
     * 上传文件方法（图片，视频，录音）
     * @param "file" 文件对象
     * @param "sendinfo"  文件信息
     * @return null
     */
    public String sendSocket() {
        String result = null;
        FileInputStream reader = null;
        DataOutputStream out = null;
        DataInputStream in = null;
        Socket socket = new Socket();
        byte[] buf = null;

        try {
            // 连接Socket
            socket.connect(new InetSocketAddress(_ADDRESS,
                    _PORT), 5000);

            // 1. 读取文件输入流
            reader = new FileInputStream(file);
            // 2. 将文件内容写到Socket的输出流中
            out = new DataOutputStream(socket.getOutputStream());
            out.writeInt(_UPLOAD);
            out.writeUTF(sendinfo);

            int bufferSize = 20480; // 20K
            buf = new byte[bufferSize];
            int read = 0;
            // 将文件输入流 循环 读入 Socket的输出流中
            while ((read = reader.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, read);
            }
            Log.i(tag, "socket执行完成");
            out.flush();
            // 一定要加上这句，否则收不到来自服务器端的消息返回
            socket.shutdownOutput();

            // //获取服务器端的相应
            in = new DataInputStream(socket.getInputStream());
            status = in.readInt();
            result = in.readUTF();
            Log.i(tag, "返回结果：" + status + "," + result);

        } catch (Exception e) {
            Log.i(tag, "socket执行异常：" + e.toString());
        } finally {
            try {
                // 结束对象
                buf = null;
                out.close();
                in.close();
                reader.close();
                socket.close();
            } catch (Exception e) {

            }
        }
        return result;
    }

}