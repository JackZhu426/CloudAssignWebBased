package Services;


import org.openstack4j.api.Builders;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.image.Image;

import java.util.List;

/**
 * If the all the Cloud Services are overloaded, CREATE a new server
 */
public class CloudService
{


    OSClientV3 os = null;


    public CloudService()
    {
        this.os = OSFactory.builderV3()
                .endpoint("https://keystone.rc.nectar.org.au:5000/v3")
                .credentials("rchtan@utas.edu.au", "MTc2NWJmN2IwMjQ0ZDI3", Identifier.byName("Default"))
                .scopeToProject(Identifier.byId("f4a468de4a9142dca0a1048f58164770"))
                .authenticate();
    }

    public String createServer()
    {
        ServerCreate server = Builders.server()
                .name("newworker")
                .flavor("cba9ea52-8e90-468b-b8c2-777a94d81ed3")
                .image("ff869ab3-4d7a-42fe-94f3-28518de62bee")
                .keypairName("raph-key").addSecurityGroup("ssh")
                .build();

        Server boot = os.compute().servers().boot(server);
        try
        {
            Thread.sleep(120000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        while (true)
        {
            String host = ListServers();
            System.out.println("new worker ip: " + host);
            if (!host.equalsIgnoreCase(""))
            {
                return host;
            }
            try
            {
                Thread.sleep(3000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

    }


    //List of all flavors
    public void ListFlavors()
    {

        List<Flavor> flavors = (List<Flavor>) os.compute().flavors().list();
        System.out.println(flavors);
    }

    //List of all images
    public void ListImages()
    {

        List<? extends Image> images = (List<? extends Image>) os.compute().images().list();
        System.out.println(images);
    }

    //List of all Servers
    public String ListServers()
    {

        List<? extends Server> servers = os.compute().servers().list();
        String ip = null;
        for (Server server : servers)
        {
            if (server.getName().equals("newworker"))
            {
                ip = server.getAccessIPv4();
            }
        }
        return ip;
    }

    //Delete a Server
    public void deleteServer()
    {
        os.compute().servers().delete("40dfe9f8-f037-48ee-b13d-6623f80b9420");
    }

//    public static void main(String[] args) throws InterruptedException
//    {
//        CloudService cloudService = new CloudService();
//
//        String host = cloudService.createServer();
//        System.out.println("outside:" + host);
////        MasterServer.sysCommand("115.146.86.166", "java -jar /home/ubuntu/javarepo/Workers.jar ", "/Users/jackzhu" +
////                "/Downloads/raph-key.pem");
//    }

}
