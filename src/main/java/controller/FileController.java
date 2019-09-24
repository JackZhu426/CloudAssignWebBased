package controller;

import com.jcraft.jsch.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Properties;
import java.util.UUID;

@Controller
public class FileController
{

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    /*
        down below: if the pram name of MultipartFile is the same as the <file name=""> submitted,
        no need to use annotation
     */
    public void upload(@RequestParam("uploadFile") MultipartFile upload, HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException
    {

        String path = "/home/ubuntu/upload/";
        String passcode = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        String fileName = passcode + "_" + upload.getOriginalFilename();
        String dir = path + fileName;
        if (fileName.endsWith(".jar"))
        {
            try
            {
                String host = "115.146.85.207";
                String user = "ubuntu";
                String privateKey = "/Users/jackzhu/Desktop/jack.pem"; //please provide your ppk file
                JSch jsch = new JSch();
                Session session = jsch.getSession(user, host, 22);
                Properties config = new Properties();
                // session.setPassword("KIT418@utas"); // if password is empty please comment it
                jsch.addIdentity(privateKey);
                System.out.println("identity added ");
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.connect();

                Channel channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftpChannel = (ChannelSftp) channel;

                OutputStream cloudOutputStream = sftpChannel.put(dir);
                InputStream localInputStream = upload.getInputStream();

                try
                {
                    int len = 0;
                    byte[] bytes = new byte[1024 * 8];
                    while ((len = localInputStream.read(bytes)) != -1)
                    {
                        cloudOutputStream.write(bytes, 0, len);
                    }
                    cloudOutputStream.close();
                    System.out.println("File Uploaded!");
                    System.out.println(dir);
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
                // passcode transmitting to the web page
                request.setAttribute("passcode", passcode);
                request.getRequestDispatcher("/success.jsp").forward(request, response);
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

        } else
        {
            request.getRequestDispatcher("/failed.jsp").forward(request, response);
        }
    }

    @RequestMapping(value = "/codeCheck", method = RequestMethod.POST)
    public void checkCode(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String userPassword = request.getParameter("userPassword");
        request.setAttribute("userPasscode", userPassword);
        request.getRequestDispatcher("/download.jsp").forward(request, response);
    }


    @RequestMapping(value = "/download")
    public static void download(HttpServletRequest request, HttpServletResponse response)
    {

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

            // get the request param
            String cloudFileName = request.getParameter("filename");
            InputStream cloudInputStream = sftpChannel.get("/home/ubuntu/result/" + cloudFileName); //server file path

            // get ServletContext
            ServletContext servletContext = request.getServletContext();
            String mimeType = servletContext.getMimeType(cloudFileName);
            // set the content type ; e.g. text/html, image/jpg
            response.setHeader("content-type", mimeType);
            // set the way of response opening
            response.setHeader("content-disposition", "attachment; filename=" + cloudFileName);
            try
            {
                ServletOutputStream outputStream = response.getOutputStream();
                byte[] bytes = new byte[1024 * 8];
                int len = 0;
                while ((len = cloudInputStream.read(bytes)) != -1)
                {
                    outputStream.write(bytes, 0, len);
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

}
