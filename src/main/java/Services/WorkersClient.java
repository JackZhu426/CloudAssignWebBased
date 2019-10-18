package Services;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WorkersClient
{


    // every millisecond fee: $1000
    private final static long FEEONMS = 1000;

    public static void main(String[] args)
    {
        List<String> list = new ArrayList<String>();
        // will automatically detect files & process every 30s
        while (true)
        {
            File filePath = new File("/home/ubuntu/upload/");
            File[] allFiles = filePath.listFiles();

            /*
                Maintain a request queue by traversing new File[] compared with those have been processed,
                find new file(s) in the directory to request for the processing
             */
            for (File file : allFiles)
            {
                System.out.println("filename: " + file.getName());
                boolean flag = false;
                if (list.contains(file.getName()))
                {
                    flag = true;
                }
                if (flag == false)
                {
                    String absolutePath = file.getAbsolutePath();
                    String fileName = file.getName();
                    System.out.println("Find a new file, prepare to process: " + fileName);
                    // multi-threaded
                    new Thread(new Runnable()
                    {
                        public void run()
                        {
                            long startTime = System.currentTimeMillis();
                            Process process = processFile(absolutePath);
                                /*
                                    total time cost, to generate the bill
                                 */
                            long timeCost = System.currentTimeMillis() - startTime;
                            long bill = timeCost * FEEONMS;
                            System.out.println("Process finished: " + fileName);
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
                                                "utf-8"), true);
                                String line = null;
                                while ((line = bufferedReader.readLine()) != null)
                                {
                                    printWriter.write(line);
                                }
                                printWriter.append("\nThis program's bill: $" + bill + ". Please pay online");
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
                list.add(file.getName());
            }

            try
            {
                Thread.sleep(30000);
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
            if (abosolutePath.endsWith(".jar"))
            {
                process = Runtime.getRuntime().exec("java -jar " + abosolutePath);
            } else if (abosolutePath.endsWith(".py"))
            {
                process = Runtime.getRuntime().exec("python " + abosolutePath);
            }


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
            String privateKey = "/home/ubuntu/javarepo/jack.pem"; //please provide your ppk file
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            jsch.addIdentity(privateKey);
            // System.out.println("identity added ");
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            try
            {
                sftpChannel.put(file, path);

                System.out.println("File Uploaded to Master: " + file.substring(file.lastIndexOf(File.separator) + 1));
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
