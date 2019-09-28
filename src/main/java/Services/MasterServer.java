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

            /*
             * Query the status of workers
             */
            for (String workerIp : workersIpList)
            {
                System.out.println("The worker ip - " + workerIp + " status is as follows: ");
                sysCommand(workerIp, "sar -u 1 1");

            }

            for (File file : files)
            {
                System.out.println("filename: " + file.getName());
                boolean flag = false;
                if (fileList.contains(file.getName()))
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
                            /*
                                call the static function upload(String file),
                                if there are multiple workers, allocate them based on its status
                                solution: traverse and get the cpu occupancy rate, .get(lowest index)
                                e.g. int lowest = 0;  int min = Double.MAX_VALUE;
                                for (i=0; i<workersIpList.size();i++) { if(processStatus(workersIpList.get(i) < min))
                                max = processStatus(workersIpList.get(i) ; lowest = i;
                             */
                            try
                            {
                                upload(filePath, workersIpList.get(0));
                            } catch (Exception e)
                            {
                                /*
                                    If worker fails, reschedule all the jobs to other workers:
                                    e.g. upload(filePath, workersIpList.get(1));
                                 */
                                // upload(filePath, workersIpList.get(1));
                            }

                        }
                    }).start();
                }
                fileList.add(file.getName());
            }

            Thread.sleep(30000);
        }
    }

    public static void upload(String file, String hostIp)
    {
        String path = "/home/ubuntu/upload/";

        try
        {
            String host = hostIp;
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
                System.out.println("File Uploaded to the worker: " + hostIp + "; filename: " + file.substring(file.lastIndexOf(
                        "/") + 1));
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

    public static void sysCommand(String hostIp, String cmd)
    {

        try
        {
            String command = cmd;
            String host = hostIp;
            String user = "ubuntu";
            String privateKey = "/home/ubuntu/javarepo/jack.pem";
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            jsch.addIdentity(privateKey);
//            System.out.println("identity added");
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
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

}
