package edu.iu.sci2.visualization.bipartitenet.component.edge;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.collect.ImmutableList;

import edu.iu.sci2.visualization.bipartitenet.PageDirector;
import edu.iu.sci2.visualization.bipartitenet.component.ComplexLabelPainter;
import edu.iu.sci2.visualization.bipartitenet.component.Paintable;
import edu.iu.sci2.visualization.bipartitenet.component.SimpleLabelPainter;
import edu.iu.sci2.visualization.bipartitenet.component.SimpleLabelPainter.XAlignment;
import edu.iu.sci2.visualization.bipartitenet.component.SimpleLabelPainter.YAlignment;
import edu.iu.sci2.visualization.bipartitenet.scale.Scale;
import edu.iu.sci2.visualization.geomaps.utility.numberformat.NumberFormatFactory;
import edu.iu.sci2.visualization.geomaps.utility.numberformat.UnsignedZeroFormat;

public class EdgeWeightLegend implements Paintable {
	private final Point2D topLeft;
	private final Scale<Double, Double> coding;
	private final ImmutableList<Double> labeledValues;
	private final ComplexLabelPainter titlePainter;
	
	private static final double EDGE_LENGTH = 40;
	private static final int LABEL_X_OFFSET = 10;
	private final Font labelFont;
	

	public EdgeWeightLegend(Point2D topLeft, ImmutableList<String> headers,
			Scale<Double,Double> coding,
			ImmutableList<Double> labeledValues,
			Font titleFont, Font labelFont) {
		this.topLeft = topLeft;
		this.coding = coding;
		this.labeledValues = labeledValues;
		this.labelFont = labelFont;
		
		this.titlePainter = new ComplexLabelPainter.Builder(topLeft, labelFont, Color.black)
			.withLineSpacing(PageDirector.LINE_SPACING)
			.addLine(headers.get(0), titleFont)
			.addLine(headers.get(1))
			.addLine(headers.get(2), labelFont, Color.gray)
			.build();
	}

	@Override
	public void paint(Graphics2D g) {
		this.titlePainter.paint(g);
		float yOffset = this.titlePainter.estimateHeight() + 8;
		paintEdges(g, yOffset);
	}

	private void paintEdges(Graphics2D gForLabels, float yOffset) {
		Graphics2D g = (Graphics2D) gForLabels.create();
		Point2D edgesTopLeft = topLeft.translate(0, yOffset);
		Point2D edgeStart = edgesTopLeft;
		
		UnsignedZeroFormat formatter = NumberFormatFactory.getNumberFormat(
				NumberFormatFactory.NumericFormatType.GENERAL, 
				ArrayUtils.toPrimitive(labeledValues.toArray(new Double[]{})));
		
		SimpleLabelPainter labelPainter = SimpleLabelPainter
				.alignedBy(XAlignment.LEFT, YAlignment.STRIKE_HEIGHT)
				.withFont(labelFont)
				.build();
		
		for (Double value : labeledValues.reverse()) {
			// loop invariant: arrowStart is the start of the current arrow
			Point2D edgeEnd = edgeStart.translate(EDGE_LENGTH, 0);
			LineSegment2D line = new LineSegment2D(edgeStart, edgeEnd);
			float lineThickness = coding.apply(value).floatValue();
			ThicknessCodedEdgeView.drawEdge(line, lineThickness, g);
			
			Point2D labelPoint = edgeEnd.translate(LABEL_X_OFFSET, 0);
			labelPainter.paintLabel(labelPoint, formatter.format(value), gForLabels);
			
			// preserve invariant
			edgeStart = edgeStart.translate(0, 1.2 * labelFont.getSize());
		}
		g.dispose();
		
	}
}
