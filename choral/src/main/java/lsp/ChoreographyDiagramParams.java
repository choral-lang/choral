package lsp;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;

public class ChoreographyDiagramParams extends TextDocumentPositionParams {
	private int helperExpansionDepth;

	public ChoreographyDiagramParams() {
	}

	public ChoreographyDiagramParams(
			TextDocumentIdentifier textDocument, Position position
	) {
		super( textDocument, position );
	}

	public ChoreographyDiagramParams(
			TextDocumentIdentifier textDocument, Position position,
			int helperExpansionDepth
	) {
		super( textDocument, position );
		this.helperExpansionDepth = helperExpansionDepth;
	}

	public int getHelperExpansionDepth() {
		return helperExpansionDepth;
	}

	public void setHelperExpansionDepth( int helperExpansionDepth ) {
		this.helperExpansionDepth = helperExpansionDepth;
	}
}
