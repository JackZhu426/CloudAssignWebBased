package Services;

import com.jcraft.jsch.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MasterServer
{

    public static void main(String[] args) throws InterruptedException
    {
        List<String> list = new ArrayList<String>();
        while (true)
        {
            File uploadFiles = new File("/home/ubuntu/upload");
            File[] files = uploadFiles.listFiles();


            for (File file : files)
            {
                System.out.println("filename: " + file.getName());
                boolean flag = false;
                if (list.contains(file.getName()))
                {
                    flag = true;
                }
                if (flag == false)
                {
                    System.out.println("Find a new file, prepare to allocate to the worker: " + file.getName());
                    final String filePath = file.getAbsolutePath();
                    // upload should be multi-threaded to achieve multiple files upload simultaneously
                    new Thread(new Runnable()
                    {

                        public void run()
                        {
                            // call the static function upload(String file)
                            upload(filePath);
                        }
                    }).start();
                }
                list.add(file.getName());
            }

            Thread.sleep(30000);
        }
    }

    public static void upload(String file)
    {
        String path = "/home/ubuntu/upload/";

        try
        {
            String host = "144.6.227.6";
            String user = "ubuntu";
            String privateKey = "/home/ubuntu/javarepo/jack.pem"; //please provide your ppk file
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
