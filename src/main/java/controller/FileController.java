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
import java.io.File;
import java.io.IOException;
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
        request.getRequestDispatcher("/success.jsp").forward(request, response);
//        return "success!";
//        } else
//        {
//        request.getRequestDispatcher("/failed.jsp").forward(request, response);
//            return "failed";
//        }
    }
}
