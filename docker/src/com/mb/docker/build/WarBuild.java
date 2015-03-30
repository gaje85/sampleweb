package com.mb.docker.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class WarBuild {
	
	static String containerId = "";
	DockerClient dockerClient = null;
	public WarBuild(){
		dockerClient = DockerClientBuilder.getInstance("http://54.164.162.19:2375").build();
		
	}
	
	public void stopExisitingContainer(){
		 if(containerId.length() > 0){
			dockerClient.stopContainerCmd(containerId).exec();
		 }else{
			 System.out.println("No Running container found");
		 }
	}
	
	public void startContainer(){
		ExposedPort tcp22 = ExposedPort.tcp(8080);
		ExposedPort tcp23 = ExposedPort.tcp(9080);

		CreateContainerResponse container = dockerClient.createContainerCmd("joseph1234/hello1")
		   .withExposedPorts(tcp22, tcp23)
		   .exec();

		Ports portBindings = new Ports();
		portBindings.bind(tcp22, Ports.Binding(8080));
		portBindings.bind(tcp23, Ports.Binding(9080));

		dockerClient.startContainerCmd(container.getId())
		   .withPortBindings(portBindings)
		   .exec();
		containerId = container.getId();
	}
	public void createImage(){
		
		try{
			 /* Output Stream - that will hold the physical TAR file */
            OutputStream tar_output = new FileOutputStream(new File("C:\\gaj\\2013\\2014\\ctsms\\shanker\\dockerfile\\tar_ball.tar"));
            /* Create Archive Output Stream that attaches File Output Stream / and specifies TAR as type of compression */
            ArchiveOutputStream my_tar_ball = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR, tar_output);
            /* Create Archieve entry - write header information*/
            File tar_input_file= new File("C:\\gaj\\2013\\2014\\ctsms\\shanker\\dockerfile\\Dockerfile");
            TarArchiveEntry tar_file = new TarArchiveEntry(tar_input_file);
            /* length of the TAR file needs to be set using setSize method */
            tar_file.setSize(tar_input_file.length());
            my_tar_ball.putArchiveEntry(tar_file);
            IOUtils.copy(new FileInputStream(tar_input_file), my_tar_ball);
            /* Close Archieve entry, write trailer information */
            my_tar_ball.closeArchiveEntry();
            /* Repeat steps for the next file that needs to be added to the TAR */
            tar_input_file= new File("C:\\gaj\\2013\\2014\\ctsms\\shanker\\dockerfile\\ctssample-1.0.0.war");
            tar_file = new TarArchiveEntry(tar_input_file);
            tar_file.setSize(tar_input_file.length());
            my_tar_ball.putArchiveEntry(tar_file);
            IOUtils.copy(new FileInputStream(tar_input_file), my_tar_ball);
            /* Close Archieve entry, write trailer information */
            my_tar_ball.closeArchiveEntry();
            my_tar_ball.finish(); 
            /* Close output stream, our files are zipped */
            tar_output.close();
		
		}catch(Exception e){
			e.printStackTrace();
		}
		File baseDir = new File("C:\\gaj\\2013\\2014\\ctsms\\shanker\\dockerfile");
		System.out.println("Done *****************************************************************");
		InputStream response = dockerClient.buildImageCmd(baseDir).withTag("joseph1234/hello1").exec();

		StringWriter logwriter = new StringWriter();

		try {
		    LineIterator itr = IOUtils.lineIterator(response, "UTF-8");
		    while (itr.hasNext()) {
		        String line = itr.next();
		        logwriter.write(line);
		       System.out.println(line);
		    }
		}catch(Exception e){
			e.printStackTrace();
		}finally {
		    IOUtils.closeQuietly(response);
		} 
	}
	
	public static void main(String[] args) {
			WarBuild wb = new WarBuild();
			wb.createImage();
			//wb.startContainer();
					
			
	}
}
