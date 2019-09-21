package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@Controller
public class FileController
{

    @RequestMapping(value = "/upload.do", method = RequestMethod.POST)
    @ResponseBody
    // @RequestParam("uploadFile")
    public String upload(MultipartFile uploadFile, HttpServletRequest request) throws IOException
    {
        String path = request.getSession().getServletContext().getRealPath("/resource/uploads");
        String fileName = uploadFile.getOriginalFilename();
        File dir = new File(path, fileName);
        if (!dir.exists() || dir.isDirectory() == false)
        {
            dir.mkdirs();
        }
        //MultipartFile自带的解析方法
        uploadFile.transferTo(dir);
        return "ok!";
    }
}
