package Services;

import java.util.List;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.image.Image;
import org.openstack4j.openstack.OSFactory;

/**
 * OpenStack Example
 */
public class CloudService
{
    OSClientV3 os = null;


    public CloudService()
    {
        this.os = OSFactory.builderV3()
                .endpoint("https://keystone.rc.nectar.org.au:5000/v3")
                .credentials("jzhu12@utas.edu.au", "YmMzZWY1NjdmNDgwMzk5", Identifier.byName("Default"))
                .scopeToProject(Identifier.byId("3c128251f80b41f48c1961748c8b78cb"))
                .authenticate();
    }

    public void createServer()
    {
        ServerCreate server = Builders.server()
                .name("Ubuntu 2")
                .flavor("cba9ea52-8e90-468b-b8c2-777a94d81ed3")
                .image("394a1b97-a8d3-4593-aab0-8156f0dfeeca")
                .keypairName("jack")
                .build();

        os.compute().servers().boot(server);
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
    public void ListServers()
    {

        List<? extends Server> servers = os.compute().servers().list();
        System.out.println(servers);
    }

    //Delete a Server
    public void deleteServer()
    {
        os.compute().servers().delete("40dfe9f8-f037-48ee-b13d-6623f80b9420");
    }


    public static void main(String[] args)
    {
        CloudService openstack = new CloudService();
        openstack.createServer();
//        openstack.ListServers();
//        openstack.ListFlavors();
//        openstack.ListImages();
    }
}
