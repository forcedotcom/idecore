package com.salesforce.ide.ui.editors.apex;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TreeViewer;

import com.salesforce.ide.ui.editors.apex.outline.ApexOutlineContentProvider;

import apex.jorje.data.Loc.RealLoc;
import apex.jorje.data.ast.ClassDecl;
import apex.jorje.data.ast.EnumDecl;
import apex.jorje.data.ast.Identifier;
import apex.jorje.data.ast.InterfaceDecl;
import apex.jorje.data.ast.BlockMember.FieldMember;
import apex.jorje.data.ast.BlockMember.InnerClassMember;
import apex.jorje.data.ast.BlockMember.InnerEnumMember;
import apex.jorje.data.ast.BlockMember.InnerInterfaceMember;
import apex.jorje.data.ast.BlockMember.MethodMember;
import apex.jorje.data.ast.BlockMember.PropertyMember;
import apex.jorje.data.ast.BlockMember.StaticStmntBlockMember;
import apex.jorje.data.ast.BlockMember.StmntBlockMember;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;

public class ApexCodeEditorMap extends ApexOutlineContentProvider {

	private static final Logger logger = Logger.getLogger(ApexCodeEditorMap.class);
	private Map<Point, MappedElement> point2Elements;
	
	public ApexCodeEditorMap() {
		point2Elements = new LinkedHashMap<Point, MappedElement>();
	}
	
	private class MappedElement {
		
		private final RealLoc loc;
		private final Object parent;
		private final Object me;
		private final boolean isClass;
		private final String name;
		private final Range elRange;
		
		public MappedElement(RealLoc loc, Object parentElement, Object myElement, String name) {
			this.loc = loc;
			// TODO: Maybe the parent will be useful someday?
			this.parent = parentElement;
			this.me = myElement;
			this.isClass = (this.me instanceof ClassDecl) || (this.me instanceof InnerClassMember);
			this.name = name;
			this.elRange = findRange(this.me);
		}
		
		private class Range {
			
			private int firstIndex, lastIndex, firstLine, lastLine;
			
			public Range(int firstIndex, int lastIndex, int firstLine, int lastLine) {
				// TODO: Maybe the indexes will be useful someday?
				this.firstIndex = firstIndex;
				this.lastIndex = lastIndex;
				this.firstLine = firstLine;
				this.lastLine = lastLine;
			}
		}
		
		public boolean isClass() {
			return this.isClass;
		}
		
		public boolean isWithinLineRange(int line) {
			return (line >= elRange.firstLine && line <= elRange.lastLine);
		}
		
		public String getName() {
			return this.name;
		}
		
		/**
		 * Find the upper and lower bounds of this element
		 * @param element
		 * @return a Range
		 */
		public Range findRange(Object element) {
			RealLoc[] realLocs = getUnsortedRealLocs(element);
			
			if (realLocs.length == 0) {
				return new Range(-1, -1, -1, -1);
			}
			
			int firstIndex, lastIndex, firstLine, lastLine;
			
			firstIndex = realLocs[0].startIndex;
			lastIndex = realLocs[0].endIndex;
			firstLine = lastLine = realLocs[0].line;
			
			for (RealLoc realLoc : realLocs) {
				if (realLoc.startIndex < firstIndex) {
					firstIndex = realLoc.startIndex;
				}
				
				if (realLoc.endIndex > lastIndex) {
					lastIndex = realLoc.endIndex;
				}
				
				if (realLoc.line < firstLine) {
					firstLine = realLoc.line;
				} else {
					lastLine = realLoc.line;
				}
			}
			
			return new Range(firstIndex, lastIndex, firstLine, lastLine);
		}
		
		/**
		 * Retrieve all RealLocs in this element
		 * @param element
		 * @return array of RealLocs
		 */
		private RealLoc[] getUnsortedRealLocs(Object element) {
			ArrayList<String> unsortedLocs = parseLocs(element);
			RealLoc[] unsortedRealLocs = convertToRealLocs(unsortedLocs);
			return unsortedRealLocs;
		}
		
		/**
		 * Parse for RealLoc substrings
		 * @param element
		 * @return list of RealLocs
		 */
		private ArrayList<String> parseLocs(Object element) {
			String wallOfText = element.toString();
			ArrayList<String> matches = new ArrayList<String>();
			Pattern regex = Pattern.compile("RealLoc\\(startIndex = [0-9]+, endIndex = [0-9]+, line = [0-9]+, column = [0-9]+\\)");
			Matcher regexMatcher = regex.matcher(wallOfText);
			while (regexMatcher.find()) {
				matches.add(regexMatcher.group());
			}
			return matches;
		}
		
		/**
		 * Create RealLocs using startIndex, endIndex, line, and column
		 * @param locs
		 * @return array of RealLocs
		 */
		private RealLoc[] convertToRealLocs(ArrayList<String> locs) {
			RealLoc[] realLocs = new RealLoc[locs.size()];
			for (int i = 0; i < locs.size(); i++) {
				String realLocString = locs.get(i).replaceAll("[^0-9]+", " ");
				List<String> digits = Arrays.asList(realLocString.trim().split(" "));
				if (digits.size() != 4) {
					continue;
				}
				realLocs[i] = new RealLoc(Integer.parseInt(digits.get(0)),
						Integer.parseInt(digits.get(1)),
						Integer.parseInt(digits.get(2)),
						Integer.parseInt(digits.get(3)));
			}
			return realLocs;
		}
	}
	
	/**
	 * Called after outline tree is refreshed. Start with root and map
	 * each element to a point.
	 * @param treeViewer
	 */
	public void mapExpandedTreeElements(TreeViewer treeViewer) {
		if (treeViewer != null) {
			Object[] expandedElements = treeViewer.getExpandedElements();
			handle(null, expandedElements[0]);
		}
	}
	
	/**
	 * Find the class or trigger where the target line lives in
	 * @param target the line to use
	 * @return a class/trigger name
	 */
	public String findEncompassingClassOrTriggerName(int target) {
		ArrayList<Point> keys = new ArrayList<Point>(point2Elements.keySet());
		for (int i = keys.size() - 1; i >= 0; i--) {
			MappedElement el = point2Elements.get(keys.get(i));
			// TODO: Handle triggers
			if (el.isClass() && el.isWithinLineRange(target)) {
				return el.getName();
			}
		}
		return null;
	}
	
	/**
	 * Map each element and its children
	 * @param parentElement
	 * @param myElement
	 */
	private void handle(Object parentElement, Object myElement) {
		if (myElement instanceof TriggerDeclUnit) {
            handle(parentElement, (TriggerDeclUnit) myElement);
        } else if (myElement instanceof ClassDecl) {
            handle(parentElement, (ClassDecl) myElement);
        } else if (myElement instanceof InterfaceDecl) {
            handle(parentElement, (InterfaceDecl) myElement);
        } else if (myElement instanceof EnumDecl) {
            handle(parentElement, (EnumDecl) myElement);
        } else if (myElement instanceof InnerClassMember) {
            handle(parentElement, (InnerClassMember) myElement);
        } else if (myElement instanceof InnerInterfaceMember) {
            handle(parentElement, (InnerInterfaceMember) myElement);
        } else if (myElement instanceof InnerEnumMember) {
            handle(parentElement, (InnerEnumMember) myElement);
        } else if (myElement instanceof StmntBlockMember) {
            handle(parentElement, (StmntBlockMember) myElement);
        } else if (myElement instanceof StaticStmntBlockMember) {
            handle(parentElement, (StaticStmntBlockMember) myElement);
        } else if (myElement instanceof Identifier) {
            handle(parentElement, (Identifier) myElement);
        } else if (myElement instanceof FieldMember) {
            handle(parentElement, (FieldMember) myElement);
        } else if (myElement instanceof MethodMember) {
            handle(parentElement, (MethodMember) myElement);
        } else if (myElement instanceof PropertyMember) {
            handle(parentElement, (PropertyMember) myElement);
        } else {
        	logger.debug("Encountered an unexpected element while mapping elements in code editor: " + myElement);
        	return;
        }
		
		Object[] children = getChildren(myElement);
		for (Object child : children) {
			handle(myElement, child);
		}
	}
	
	private void handle(Object parentElement, TriggerDeclUnit myElement) {
		RealLoc loc = (RealLoc) myElement.name.loc;
		String myName = myElement.name.value;
		MappedElement element = new MappedElement(loc, parentElement, (TriggerDeclUnit) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}
	
	private void handle(Object parentElement, ClassDecl myElement) {
		RealLoc loc = (RealLoc) myElement.name.loc;
		String myName = myElement.name.value;
		MappedElement element = new MappedElement(loc, parentElement, (ClassDecl) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}
	
	private void handle(Object parentElement, InterfaceDecl myElement) {
		RealLoc loc = (RealLoc) myElement.name.loc;
		String myName = myElement.name.value;
		MappedElement element = new MappedElement(loc, parentElement, (InterfaceDecl) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}
	
	private void handle(Object parentElement, EnumDecl myElement) {
		RealLoc loc = (RealLoc) myElement.name.loc;
		String myName = myElement.name.value;
		MappedElement element = new MappedElement(loc, parentElement, (EnumDecl) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}
	
	private void handle(Object parentElement, InnerClassMember myElement) {
		RealLoc loc = (RealLoc) myElement.body.name.loc;
		String myName = myElement.body.name.value;
		MappedElement element = new MappedElement(loc, parentElement, (InnerClassMember) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}
	
	private void handle(Object parentElement, InnerInterfaceMember myElement) {
		RealLoc loc = (RealLoc) myElement.body.name.loc;
		String myName = myElement.body.name.value;
		MappedElement element = new MappedElement(loc, parentElement, (InnerInterfaceMember) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}
	
	private void handle(Object parentElement, InnerEnumMember myElement) {
		RealLoc loc = (RealLoc) myElement.body.name.loc;
		String myName = myElement.body.name.value;
		MappedElement element = new MappedElement(loc, parentElement, (InnerEnumMember) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}
	
	private void handle(Object parentElement, StmntBlockMember myElement) {
		RealLoc loc = (RealLoc) myElement.loc;
		String myName = "";
		MappedElement element = new MappedElement(loc, parentElement, (StmntBlockMember) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}
	
	private void handle(Object parentElement, StaticStmntBlockMember myElement) {
		RealLoc loc = (RealLoc) myElement.loc;
		String myName = "";
		MappedElement element = new MappedElement(loc, parentElement, (StaticStmntBlockMember) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}
	
	private void handle(Object parentElement, Identifier myElement) {
		RealLoc loc = (RealLoc) myElement.loc;
		String myName = myElement.value;
		MappedElement element = new MappedElement(loc, parentElement, (Identifier) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}
	
	private void handle(Object parentElement, FieldMember myElement) {
		RealLoc loc = (RealLoc) myElement.variableDecls.decls.get(0).name.loc;
		String myName = myElement.variableDecls.decls.get(0).name.value;
		MappedElement element = new MappedElement(loc, parentElement, (FieldMember) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}

	private void handle(Object parentElement, MethodMember myElement) {
		RealLoc loc = (RealLoc) myElement.methodDecl.name.loc;
		String myName =  myElement.methodDecl.name.value;
		MappedElement element = new MappedElement(loc, parentElement, (MethodMember) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}
	
	private void handle(Object parentElement, PropertyMember myElement) {
		RealLoc loc = (RealLoc) myElement.propertyDecl.name.loc;
		String myName =  myElement.propertyDecl.name.value;
		MappedElement element = new MappedElement(loc, parentElement, (PropertyMember) myElement, myName);
		point2Elements.put(new Point(loc.line, loc.column), element);
	}
}
