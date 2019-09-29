package controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
//import Lastfinding.scalability.*;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class ServerFileReading
{

    public static void runCommand()
    {

        try
        {
            String command = "sar -q 1 1";
            String host = "144.6.227.6";
            String user = "ubuntu";
            String privateKey = "/Users/jackzhu/Desktop/jack.pem";
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            // session.setPassword("utas");
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
                        System.out.println("runq-sz: " + line.substring(8, 21).trim());
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
        }
    }

    public static void downloadFile()
    {

        try
        {
            String host = "144.6.227.17";
            String user = "ubuntu";
            String privateKey = "C:/ .x"; //please provide your ppk file
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            // session.setPassword("KIT418@utas"); ////if password is empty please comment it
            jsch.addIdentity(privateKey);
            System.out.println("connected to the host: " + host);
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            BufferedWriter writer = new BufferedWriter(new FileWriter("C:/Users/sudheerb/Documents/serveroutput.java"
                    , true)); //local path to store downloaded file

            InputStream stream = sftpChannel.get("/home/ubuntu/eclipse-workspace/FogPushNew2" +
                    ".zip_expanded/FogPushNew2/src/FogServer.java"); //server file path

            try
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = br.readLine()) != null)
                {
                    writer.append(line);
                }
                System.out.println("File Downloaded");
                writer.close();
            } catch (IOException io)
            {
                System.out.println("Exception occurred during reading file from SFTP server due to " + io.getMessage());
                io.getMessage();

            } catch (Exception e)
            {
                System.out.println("Exception occurred during reading file from SFTP server due to " + e.getMessage());
                e.getMessage();

            }

            sftpChannel.exit();
            session.disconnect();
        } catch (JSchException e)
        {
            e.printStackTrace();
        } catch (SftpException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public static void processCommand(String Command)
    {
        try
        {

            // create a new process
            System.out.println("Creating Process");

            ProcessBuilder builder = new ProcessBuilder(Command);
            Process pro = builder.start();

            // wait 10 seconds
            System.out.println("Waiting");
            Thread.sleep(10000);

            // kill the process
            pro.destroy();
            System.out.println("Process destroyed");

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args)
    {
        // TODO Auto-generated method stub
        runCommand();
        //processCommand("notepad.exe");
//        downloadFile();

    }

}
