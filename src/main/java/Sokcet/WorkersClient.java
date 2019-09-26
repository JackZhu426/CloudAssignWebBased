package Sokcet;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.Properties;

public class WorkersClient
{
    public static void main(String[] args)
    {
        int totalFiles = 0;

        // will automatically detect files & process every 60s
        while (true)
        {
            File filePath = new File("/home/ubuntu/upload/");
            File[] allFiles = filePath.listFiles();
            for (File file : allFiles)
            {
                System.out.println("filename: " + file.getName());
            }
            if (allFiles.length > totalFiles)
            {
                for (int i = totalFiles; i < allFiles.length; i++)
                {
                    String absolutePath = allFiles[i].getAbsolutePath();
                    String fileName = allFiles[i].getName();
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Process process = processFile(absolutePath);
                            // result file (e.g. 1gsh457j_result.txt)
                            String resultFileName = "/home/ubuntu/result/" + fileName.split("_")[0] +
                                    "_result" +
                                    ".txt";
                            try
                            {
                                BufferedReader bufferedReader =
                                        new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"
                                        ));
                                PrintWriter printWriter =
                                        new PrintWriter(new OutputStreamWriter(new FileOutputStream(resultFileName),
                                                "utf-8"));
                                String line = null;
                                while ((line = bufferedReader.readLine()) != null)
                                {
                                    printWriter.write(line);
                                }
                                // close the streams
                                bufferedReader.close();
                                printWriter.close();
                                // upload back to the master
                                uploadResult(resultFileName);
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
                totalFiles = allFiles.length;
            }
            try
            {
                Thread.sleep(60000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static Process processFile(String abosolutePath)
    {
        Process process = null;
        try
        {
            process = Runtime.getRuntime().exec("java -jar " + abosolutePath);

        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            return process;
        }
    }

    public static void uploadResult(String file)
    {
        String path = "/home/ubuntu/result/";

        try
        {
            String host = "115.146.85.207";
            String user = "ubuntu";
            // TODO: need to change the path
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
