package dev.cyberjar.embabeldemo.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.data.geo.Point;

import java.io.IOException;


public class PointFromXYDeserializer extends StdDeserializer<Point> {

    public PointFromXYDeserializer() {
        super(Point.class);
    }

    @Override
    public Point deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = (JsonNode) p.readValueAsTree(); // Jackson 3: no getCodec()

        double x = node.path("x").asDouble();
        double y = node.path("y").asDouble();
        return new Point(x, y);
    }
}
