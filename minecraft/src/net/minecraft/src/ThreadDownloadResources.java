package net.minecraft.src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.minecraft.client.Minecraft;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ThreadDownloadResources extends Thread {
	public File resourcesFolder;
	private Minecraft mc;
	private boolean closing = false;

	public ThreadDownloadResources(File var1, Minecraft var2) {
		this.mc = var2;
		this.setName("Resource download thread");
		this.setDaemon(true);
		File var3 = new File(var1, "resources/");
		if(var3.exists()) {
			System.out.println("Found a resources folder! Path: " + var3.getAbsolutePath());
			this.resourcesFolder = var3;
		} else {
			File var4 = findOggFile(var1);
			if(var4 != null) {
				System.out.println("Found an OGG resource! Path: " + var4.getAbsolutePath());
				this.resourcesFolder = var4.getParentFile();
			} else {
				this.resourcesFolder = var3;
			}
		}
		if(!this.resourcesFolder.exists() && !this.resourcesFolder.mkdirs()) {
			throw new RuntimeException("The working directory could not be created: " + this.resourcesFolder);
		}
	}

	private File findOggFile(File var1) {
		File[] var2 = var1.listFiles();
		if(var2 == null) return null;
		for(int i = 0; i < var2.length; ++i) {
			if(var2[i].isDirectory()) {
				File var3 = findOggFile(var2[i]);
				if(var3 != null) return var3;
			} else if(var2[i].getName().toLowerCase().endsWith(".ogg")) {
				return var2[i];
			}
		}
		return null;
	}

	public void run() {
		try {
			URL var1 = new URL("https://s3.amazonaws.com/MinecraftResources/");
			DocumentBuilderFactory var2 = DocumentBuilderFactory.newInstance();
			DocumentBuilder var3 = var2.newDocumentBuilder();
			Document var4 = var3.parse(var1.openStream());
			NodeList var5 = var4.getElementsByTagName("Contents");

			for(int var6 = 0; var6 < 2; ++var6) {
				for(int var7 = 0; var7 < var5.getLength(); ++var7) {
					Node var8 = var5.item(var7);
					if(var8.getNodeType() == 1) {
						Element var9 = (Element)var8;
						String var10 = ((Element)var9.getElementsByTagName("Key").item(0)).getChildNodes().item(0).getNodeValue();
						long var11 = Long.parseLong(((Element)var9.getElementsByTagName("Size").item(0)).getChildNodes().item(0).getNodeValue());
						if(var11 > 0L) {
							this.downloadAndInstallResource(var1, var10, var11, var6);
							if(this.closing) {
								return;
							}
						}
					}
				}
			}
		} catch (Exception var13) {
			var13.printStackTrace();
		} finally {
			this.loadResource(this.resourcesFolder, "");
		}

	}

	public void reloadResources() {
		this.loadResource(this.resourcesFolder, "");
	}

	private void loadResource(File var1, String var2) {
		File[] var3 = var1.listFiles();
		if(var3 == null) return;

		for(int var4 = 0; var4 < var3.length; ++var4) {
			if(var3[var4].isDirectory()) {
				this.loadResource(var3[var4], var2 + var3[var4].getName() + "/");
			} else {
				try {
					this.mc.installResource(var2 + var3[var4].getName(), var3[var4]);
				} catch (Exception var6) {
					System.out.println("Failed to add " + var2 + var3[var4].getName());
				}
			}
		}

	}

	private void downloadAndInstallResource(URL var1, String var2, long var3, int var5) {
		try {
			int var6 = var2.indexOf("/");
			String var7 = var2.substring(0, var6);
			if(!var7.equals("sound") && !var7.equals("newsound")) {
				if(var5 != 1) {
					return;
				}
			} else if(var5 != 0) {
				return;
			}

			File var8 = new File(this.resourcesFolder, var2);
			if(!var8.exists() || !isOggResource(var8, var7)) {
				var8.getParentFile().mkdirs();
				String var9 = var2.replaceAll(" ", "%20");
				File var10 = new File(var8.getPath() + ".tmp");
				try {
					this.downloadResource(new URL(var1, var9), var10, var3);
					if(!isOggResource(var10, var7)) {
						if(var10.exists()) var10.delete();
						return;
					}
				} catch (IOException var11) {
					if(var10.exists()) var10.delete();
					return;
				}
				if(var8.exists() && !var8.delete()) {
					throw new IOException("Could not replace cached resource " + var8);
				}
				if(!var10.renameTo(var8)) {
					throw new IOException("Could not replace cached resource " + var8);
				}
				if(this.closing) {
					return;
				}
			}

			this.mc.installResource(var2, var8);
		} catch (Exception var10) {
			var10.printStackTrace();
		}

	}

	private boolean isOggResource(File var1, String var2) {
		if(!var2.equals("sound") && !var2.equals("newsound")) return true;
		try {
			DataInputStream var3 = new DataInputStream(new java.io.FileInputStream(var1));
			int var4 = var3.readInt();
			var3.close();
			return var4 == 1332176723;
		} catch (IOException var5) {
			return false;
		}
	}

	private void downloadResource(URL var1, File var2, long var3) throws IOException {
		byte[] var5 = new byte[4096];
		DataInputStream var6 = new DataInputStream(var1.openStream());
		DataOutputStream var7 = new DataOutputStream(new FileOutputStream(var2));
		boolean var8 = false;

		do {
			int var9 = var6.read(var5);
			if(var9 < 0) {
				var6.close();
				var7.close();
				return;
			}

			var7.write(var5, 0, var9);
		} while(!this.closing);

	}

	public void closeMinecraft() {
		this.closing = true;
	}
}
