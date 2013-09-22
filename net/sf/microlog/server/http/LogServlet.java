package net.sf.microlog.server.http;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogServlet extends HttpServlet {

	private static final long serialVersionUID = 7972650316464534386L;

	private byte[] buffer = new byte[1024];

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		InputStream is = req.getInputStream();

		int contentLength = req.getContentLength();
		if (contentLength > buffer.length) {
			buffer = new byte[contentLength + 1024];
		}

		for (int index = 0; index < contentLength; index++) {
			is.read(buffer);
		}

		System.out.println(new String(buffer, 0, contentLength));
	}

}