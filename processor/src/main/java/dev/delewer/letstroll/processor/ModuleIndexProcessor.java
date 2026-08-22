package dev.delewer.letstroll.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes(ModuleIndexProcessor.ANNOTATION)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class ModuleIndexProcessor extends AbstractProcessor {

    static final String ANNOTATION = "dev.delewer.letstroll.module.TrollModule";
    private static final String SERVICE = "dev.delewer.letstroll.module.LetsTrollModule";

    private final Set<String> discovered = new LinkedHashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        TypeElement marker = processingEnv.getElementUtils().getTypeElement(ANNOTATION);
        if (marker != null) {
            for (Element element : round.getElementsAnnotatedWith(marker)) {
                if (!isUsable(element)) {
                    continue;
                }
                discovered.add(processingEnv.getElementUtils()
                        .getBinaryName((TypeElement) element)
                        .toString());
            }
        }
        if (round.processingOver() && !discovered.isEmpty()) {
            writeIndex();
        }
        return false;
    }

    private boolean isUsable(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            report(element, "@TrollModule can only be placed on a class");
            return false;
        }
        if (element.getModifiers().contains(Modifier.ABSTRACT)) {
            report(element, "@TrollModule class must not be abstract");
            return false;
        }
        boolean hasDefaultConstructor = element.getEnclosedElements().stream()
                .filter(member -> member.getKind() == ElementKind.CONSTRUCTOR)
                .anyMatch(member -> member.getModifiers().contains(Modifier.PUBLIC)
                        && ((javax.lang.model.element.ExecutableElement) member).getParameters().isEmpty());
        if (!hasDefaultConstructor) {
            report(element, "@TrollModule class needs a public no-argument constructor");
            return false;
        }
        return true;
    }

    private void writeIndex() {
        try {
            Writer writer = processingEnv.getFiler()
                    .createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + SERVICE)
                    .openWriter();
            try (writer) {
                for (String name : discovered) {
                    writer.write(name);
                    writer.write(System.lineSeparator());
                }
            }
        } catch (IOException exception) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Unable to write module index: " + exception.getMessage());
        }
    }

    private void report(Element element, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
