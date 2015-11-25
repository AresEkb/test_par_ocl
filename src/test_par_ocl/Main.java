package test_par_ocl;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.ocl.pivot.ExpressionInOCL;
import org.eclipse.ocl.pivot.uml.UMLStandaloneSetup;
import org.eclipse.ocl.pivot.utilities.OCL;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.eclipse.ocl.xtext.essentialocl.EssentialOCLStandaloneSetup;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;

public class Main {

	public static void main(String[] args) {
		final String input = "model/my.uml";

		ResourceSet rs = new ResourceSetImpl();

		System.out.println("Initialization");
		rs.setURIConverter(new CustomURIConverter());
		System.out.println("  UML");
		UMLResourcesUtil.init(rs);
		System.out.println("  OCL");
		EssentialOCLStandaloneSetup.doSetup();
		UMLStandaloneSetup.init();

		try {
			System.out.println("Loading model " + input);
			Resource resource = rs.getResource(createFileURI(input), true);

			EObject model = resource.getContents().get(0);

			// Uncomment the following lines and everything will work
//			OCL ocl = OCL.newInstance();
//			ocl.getMetamodelManager().getASOf(org.eclipse.ocl.pivot.Package.class, model);

			IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList()).parallelStream().forEach(i -> {
				parseConstraints(model);
			});

			System.out.println("Done!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static URI createFileURI(String relativePath) {
		return URI.createFileURI(new File(relativePath).getAbsolutePath());
	}

	private static void parseConstraints(EObject model) {
		model.eAllContents().forEachRemaining(obj -> {
			if (obj instanceof Constraint) {
				OCL ocl = OCL.newInstance();
				ExpressionInOCL expr = null;
				try {
					org.eclipse.ocl.pivot.Constraint asConstraint = ocl.getMetamodelManager()
							.getASOf(org.eclipse.ocl.pivot.Constraint.class, obj);
					expr = ocl.getSpecification(asConstraint);
					System.out.println(expr);
				} catch (ParserException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
