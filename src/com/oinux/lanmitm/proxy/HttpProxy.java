package com.oinux.lanmitm.proxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicRequestLine;

import android.provider.CalendarContract.Instances;
import android.util.Log;

import com.oinux.lanmitm.AppContext;
import com.oinux.lanmitm.util.RequestParser;

/**
 * 
 * @author oinux
 *
 */
public class HttpProxy extends Thread {

	private final static int HTTP_SERVER_PORT = 80;
	public static final int HTTP_PROXY_PORT = 8080;
	private static final int BACKLOG = 255;
	public static boolean stop = true;
	private static final Pattern PATTERN = Pattern
			.compile(
					"(\\.css\\??|\\.js\\??|\\.jpg\\??|\\.gif\\??|\\.png\\??|\\.jpeg\\??)",
					Pattern.CASE_INSENSITIVE);

	private ServerSocket mServerSocket;
	private OnRequestListener mOnRequestListener;
	private ExecutorService executor;
	private String inject;

	private static HttpProxy instance;

	public static HttpProxy getInstance() {
		if (instance == null || instance.getState() == State.TERMINATED)
			instance = new HttpProxy();
		return instance;
	}

	private HttpProxy() {
	}

	public String getInject() {
		return inject;
	}

	public void setInject(String inject) {
		this.inject = inject;
	}

	public OnRequestListener getOnRequestListener() {
		return mOnRequestListener;
	}

	public void setOnRequestListener(OnRequestListener onRequestListener) {
		this.mOnRequestListener = onRequestListener;
	}

	@Override
	public synchronized void start() {
		if (this.getState() == State.NEW)
			super.start();
	}

	@Override
	public void run() {
		try {
			mServerSocket = new ServerSocket();
			mServerSocket.setReuseAddress(true);
			mServerSocket.bind(
					new InetSocketAddress(AppContext.getInetAddress(),
							HTTP_PROXY_PORT), BACKLOG);
			executor = Executors.newCachedThreadPool();
			while (!stop) {
				Socket client = mServerSocket.accept();
				executor.execute(new DealThread(client, mOnRequestListener));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (mServerSocket != null) {
				try {
					mServerSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (executor != null) {
				executor.shutdownNow();
			}
		}
	}
	
	class DeepDealThread extends DealThread{

		public DeepDealThread(Socket client,
				OnRequestListener onRequestListener) {
			super(client, onRequestListener);
		}
		
		@Override
		public void run() {
			
		}
	}

	class DealThread extends Thread {

		private OnRequestListener onRequestListener;
		private Socket clientSocket;
		private Socket serverSocket;
		private BufferedOutputStream writer = null;
		private InputStream serverReader = null;
		private OutputStream serverWriter = null;

		public DealThread(Socket client, OnRequestListener onRequestListener) {
			this.onRequestListener = onRequestListener;
			this.clientSocket = client;
		}

		@Override
		public void run() {
			byte[] buffer = new byte[1024];
			int read = 0;
			InputStream inputStream = null;

			try {
				inputStream = clientSocket.getInputStream();
				final String clientIp = clientSocket.getInetAddress()
						.getHostAddress();

				if ((read = inputStream.read(buffer, 0, 1024)) >= 0) {
					ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
							buffer, 0, read);
					BufferedReader bReader = new BufferedReader(
							new InputStreamReader(byteArrayInputStream));
					StringBuilder builder = new StringBuilder();
					String line = null, serverName = null, path = null;
					boolean found = false, foundPath = false, ok = true;
					ArrayList<String> headers = new ArrayList<String>();

					while ((line = bReader.readLine()) != null) {
						headers.add(line);
						if (!found && line.contains("Host")) {
							serverName = line.substring(5).trim();
							found = true;
						}
						if (!foundPath) {
							path = line;
							foundPath = true;
							Matcher m = PATTERN.matcher(path);
							if (m.find())
								ok = false;
						}
						builder.append(line + "\r\n");
					}
					builder.append("\r\n");
					if (serverName != null) {
						String request = builder.toString();
						serverSocket = new Socket(serverName, HTTP_SERVER_PORT);
						serverSocket.setSoTimeout(1000);

						if (onRequestListener != null && ok) {
							onRequestListener.onRequest(clientIp, serverName,
									serverSocket.getInetAddress()
											.getHostAddress(), path, headers);
						}

						writer = new BufferedOutputStream(
								clientSocket.getOutputStream());

						serverReader = serverSocket.getInputStream();
						serverWriter = serverSocket.getOutputStream();

						serverWriter.write(request.getBytes());
						serverWriter.flush();
						
						byte[] buff = new byte[1024];
						int len = -1;
						while ((len = serverReader.read(buff, 0, 1024)) >= 0) {
							writer.write(buff, 0, len);
							writer.flush();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (inputStream != null) {
						inputStream.close();
					}
					if (writer != null) {
						if (inject != null) {
							writer.flush();
							writer.write(inject.getBytes());
							writer.flush();
						}
						writer.close();
					}
					if (serverReader != null) {
						serverReader.close();
					}
					if (serverWriter != null) {
						serverWriter.close();
					}
					if (clientSocket != null)
						clientSocket.close();
					if (serverSocket != null)
						serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public interface OnRequestListener {
		public void onRequest(String clientId, String hostname,
				String serverIp, String path, ArrayList<String> headers);
	}
}
