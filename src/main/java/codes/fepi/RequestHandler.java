package codes.fepi;

import codes.fepi.dto.ActionDto;
import codes.fepi.dto.NotificationDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static spark.Spark.*;

public class RequestHandler {
	private final ObjectMapper mapper;
	private final Logger LOGGER;

	private volatile List<NotificationDto> notifications;
	private ActionDto pendingAction;

	public RequestHandler() {
		mapper = new ObjectMapper();
		LOGGER = LoggerFactory.getLogger(RequestHandler.class);
		notifications = new ArrayList<>(0);
		pendingAction = new ActionDto();
	}

	public void setup() {
		exception(IllegalArgumentException.class, (exception, request, response) -> {
			response.status(400);
			response.body(exception.getMessage());
		});
		get("/", this::getNotification);
		post("/", this::postNotification);
		post("/action", this::postAction);
	}

	private Object getNotification(Request req, Response res) {
		return writeDto(notifications);
	}

	private Object postNotification(Request req, Response res) {
		notifications = Arrays.asList(readDto(req, NotificationDto[].class));
		synchronized (this) {
			if(pendingAction.hasActions()) {
				String dto = writeDto(pendingAction);
				pendingAction = new ActionDto();
				return dto;
			}
		}
		return "";
	}

	private Object postAction(Request req, Response res) {
		ActionDto dto = readDto(req, ActionDto.class);
		synchronized (this) {
			pendingAction.addAction(dto);
		}
		return "ok";
	}

	private <T> T readDto(Request req, Class<T> clazz) {
		try {
			return mapper.readValue(req.body(), clazz);
		} catch (IOException e) {
			throw new IllegalArgumentException("Invalid dto");
		}
	}

	private String writeDto(Object o) {
		try {
			return mapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Something went wrong serializing a dto");
	}
}
