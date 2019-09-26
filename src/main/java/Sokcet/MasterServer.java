package Sokcet;

import com.jcraft.jsch.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.UUID;

public class MasterServer
{
    public static void main(String[] args) throws InterruptedException
    {
        int totalFiles = 0;
        while (true)
        {
            File uploadFiles = new File("/Users/jackzhu/Desktop/PTE");
            File[] files = uploadFiles.listFiles();


            for (File file : files)
            {
                System.out.println("filename: " + file.getName());
            }
            if (files.length > totalFiles)
            {
                for (int i = totalFiles; i < files.length; i++)
                {
                    String filePath = files[i].getAbsolutePath();
                    // upload should be multi-threaded to achieve multiple files upload simultaneously
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // call the static function upload(String file)
                            upload(filePath);
                        }
                    }).start();
                }
                totalFiles = files.length;
            }

            Thread.sleep(60000);
        }
    }

    public static void upload(String file)
    {
        String path = "/home/ubuntu/upload/";

        try
        {
            String host = "115.146.85.207";
            String user = "ubuntu";
            String privateKey = "/Users/jackzhu/Desktop/jack.pem"; //please provide your ppk file
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            jsch.addIdentity(privateKey);
            System.out.println("identity added ");
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            try
            {
                sftpChannel.put(file, path);
//                System.out.println("gethome: " + sftpChannel.getHome());
//                System.out.println("getBulkRequests: " + sftpChannel.getBulkRequests());
//                System.out.println("lstat: " + sftpChannel.lstat(path));
//                System.out.println("ls: " + sftpChannel.ls(path));
//                System.out.println("stat: " + sftpChannel.stat(path));
//                System.out.println("statVFS: " + sftpChannel.statVFS(path));
                System.out.println("File Uploaded!");
            } catch (Exception e)
            {
                System.out.println("Exception occurred during reading file from SFTP server due to " + e
                        .getMessage());
                e.getMessage();

            }

            sftpChannel.exit();
            session.disconnect();
        } catch (JSchException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            System.out.println(e);
        }


    }
}
