package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@Controller
public class FileController
{

    @RequestMapping(value = "/upload.do", method = RequestMethod.POST)
//    @ResponseBody
    // @RequestParam("uploadFile")
    public void upload(@RequestParam("uploadFile") MultipartFile upload, HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException
    {
        String path = request.getSession().getServletContext().getRealPath("/resource/uploads");
        String fileName =
                UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8) + upload.getOriginalFilename();
        File dir = new File(path, fileName);
        if (!dir.exists() || dir.isDirectory() == false)
        {
            dir.mkdirs();
        }

//        if (fileName.endsWith(".jar"))
//        {
        //MultipartFile自带的解析方法
        upload.transferTo(dir);
        // run .jar
        Process process = Runtime.getRuntime().exec("java -jar" + dir.getAbsolutePath());
        String resultOfJar = loadInputStream(process.getInputStream());
        request.setAttribute("resultOfJar", resultOfJar);
        request.getRequestDispatcher("/success.jsp").forward(request, response);
//        return "success!";
//        } else
//        {
//        request.getRequestDispatcher("/failed.jsp").forward(request, response);
//            return "failed";
//        }
    }

    private String loadInputStream(InputStream in) throws IOException
    {

        int len = 0;
        BufferedInputStream bufferedIn = new BufferedInputStream(in);
        StringBuffer sb = new StringBuffer();
        while ((len = bufferedIn.read()) != -1)
        {
            sb.append((char) len);
        }
        bufferedIn.close();
        return sb.toString();
    }
}
