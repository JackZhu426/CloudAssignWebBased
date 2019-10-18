package Services;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MasterServer
{

    public static void main(String[] args) throws InterruptedException
    {
        List<String> fileList = new ArrayList<String>();
        // different ips of workers
        List<String> workersIpList = new ArrayList<String>();
        workersIpList.add("144.6.227.6");
        while (true)
        {
            File uploadFiles = new File("/home/ubuntu/upload");
            File[] files = uploadFiles.listFiles();
            int fileNum = 0;
            for (File file : files)
            {
                fileNum++;
            }
            /*
             * Query the status of workers
             */
            for (int i = 0; i < workersIpList.size(); i++)
            {
                System.out.println("The worker ip - " + workersIpList.get(i) + " status is as follows: ");
                if (i == 0)
                {
                    sysCommand(workersIpList.get(i), "vmstat", "/home/ubuntu/javarepo/jack.pem");
                } else if (i == 1)
                {
                    sysCommand(workersIpList.get(i), "vmstat", "/home/ubuntu/javarepo/raph-key.pem");
                }

            }
            /*
                Elasticity: start a new machine to reduce the waiting time
             */
            boolean flagQueue = true; // default
            for (String workerIp : workersIpList)
            {
                // if there is 1 server's waiting processes is < 10, no need to create a new server
                if (fileList.size() + 1 < fileNum)
                {
                    flagQueue = false;
                }
            }
            // all queues are busy (more than 10 processes waiting)
            if (flagQueue == false)
            {
                CloudService cloudService = new CloudService();
                // in this process
                String serverIp = cloudService.createServer();
                    /*
                        add to the list & run the jar file to start the program
                     */
                workersIpList.add(serverIp);
                Thread.sleep(60000);

                createWorker(serverIp, "java -jar /home/ubuntu/javarepo/Workers.jar", "/home/ubuntu/javarepo" +
                        "/raph" +
                        "-key.pem");

            }

            for (File file : files)
            {
                System.out.println("filename: " + file.getName());

                boolean flagFileExsists = false;
                if (fileList.contains(file.getName()))
                {
                    flagFileExsists = true;
                }
                if (flagFileExsists == false)
                {
                    System.out.println("Find a new file, prepare to allocate to the worker: " + file.getName());
                    String filePath = file.getAbsolutePath();
                    // upload should be multi-threaded to achieve multiple files upload simultaneously
                    new Thread(new Runnable()
                    {

                        public void run()
                        {
                            /*
                                call the static function upload(String file),
                                if there are multiple workers, allocate them based on its status
                                solution: traverse and get the cpu occupancy rate, .get(lowest index)
                                e.g. (pseudocode) int lowest = 0;  int min = Double.MAX_VALUE;
                                for (i=0; i<workersIpList.size();i++) { if(waitingProcess(workersIpList.get(i) < min))
                                max = processStatus(workersIpList.get(i) ; lowest = i;
                             */

                            if (workersIpList.size() == 2)
                            {
                                try
                                {
                                    upload(filePath, workersIpList.get(0), "/home/ubuntu/javarepo/jack.pem");
                                } catch (Exception e)
                                {
                                    try
                                    {
                                        upload(filePath, workersIpList.get(1), "/home/ubuntu/javarepo/raph-key.pem");
                                    } catch (Exception ex)
                                    {
                                        ex.printStackTrace();
                                    }
                                }
                                try
                                {
                                    upload(filePath, workersIpList.get(1), "/home/ubuntu/javarepo/raph-key.pem");
                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            } else
                            {
                                try
                                {
                                    upload(filePath, workersIpList.get(0), "/home/ubuntu/javarepo/jack.pem");
                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }


                        }
                    }).start();
                }
                fileList.add(file.getName());
            }

            Thread.sleep(30000);
        }
    }

    public static void upload(String file, String hostIp, String pk) throws Exception
    {
        String path = "/home/ubuntu/upload/";


        String host = hostIp;
        String user = "ubuntu";
        String privateKey = pk; // in Linux, provide .pem file
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

        sftpChannel.put(file, path);
        System.out.println("File Uploaded to the worker: " + hostIp + "; filename: " + file.substring(file.lastIndexOf(
                "/") + 1));


        sftpChannel.exit();
        session.disconnect();


    }

    public static void sysCommand(String hostIp, String cmd, String pk)
    {

        try
        {
            String command = cmd;
            String host = hostIp;
            String user = "ubuntu";
            String privateKey = pk;
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            jsch.addIdentity(privateKey);
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            channel.setInputStream(System.in);
            channel.connect();

            InputStream input = channel.getInputStream();
            try
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = br.readLine()) != null)
                {
                    System.out.println(line);
                }

            } catch (IOException io)
            {
                System.out.println("Exception occurred during reading file from SFTP server due to " + io.getMessage());
                io.getMessage();

            } catch (Exception e)
            {
                System.out.println("Exception occurred during reading file from SFTP server due to " + e.getMessage());
                e.getMessage();
            }
            channel.disconnect();
            session.disconnect();
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public static void createWorker(String hostIp, String cmd, String pk)
    {

        try
        {
            String command = cmd;
            String host = hostIp;
            String user = "ubuntu";
            String privateKey = pk;
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            jsch.addIdentity(privateKey);
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            channel.connect();

            channel.disconnect();
            session.disconnect();
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public static String waitingProcess(String hostIp)
    {

        String queueNum = null;

        try
        {
            String command = "sar -q 1 1";
            String host = hostIp;
            String user = "ubuntu";
            String privateKey = "/home/ubuntu/javarepo/jack.pem";
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            jsch.addIdentity(privateKey);
            System.out.println("connected to the host: " + host);
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            //channel.setInputStream(null);
            //((ChannelExec)channel).setErrStream(System.err);
            channel.setInputStream(System.in);
            channel.connect();

            InputStream input = channel.getInputStream();
            try
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = br.readLine()) != null)
                {
                    System.out.println(line);
                    if (line.contains("Average"))
                    {
                        queueNum = line.substring(8, 21).trim();
                        System.out.println("runq-sz: " + queueNum);
                    }
                }

            } catch (IOException io)
            {
                System.out.println("Exception occurred during reading file from SFTP server due to " + io.getMessage());
                io.getMessage();

            } catch (Exception e)
            {
                System.out.println("Exception occurred during reading file from SFTP server due to " + e.getMessage());
                e.getMessage();

            }
        } catch (Exception e)
        {
            System.out.println(e);
        } finally
        {
            return queueNum;
        }
    }


}
