package com.ashigeru.eclipse.util.launch.internal.ui.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch {@link IResource}s as operating system executables associated with their files.
 */
public class LaunchResourceHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (event != null) {
            List<IPath> locations = detectSelectedPaths(event);
            for (IPath path : locations) {
                Program.launch(path.toOSString());
            }
        }
        return null;
    }

    private List<IPath> detectSelectedPaths(ExecutionEvent event)
            throws ExecutionException {
        assert event != null;
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        if (selection == null || selection.isEmpty()) {
            return Collections.emptyList();
        }
        if ((selection instanceof IStructuredSelection) == false) {
            return Collections.emptyList();
        }
        Iterator<?> iter = ((IStructuredSelection) selection).iterator();
        List<IPath> locations = new ArrayList<IPath>();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if ((obj instanceof IAdaptable) == false) {
                continue;
            }
            IAdaptable adaptable = (IAdaptable) obj;
            IResource resource = (IResource) adaptable.getAdapter(IResource.class);
            if (resource == null) {
                continue;
            }
            IPath location = resource.getLocation();
            if (location == null) {
                continue;
            }
            locations.add(location);
        }
        return locations;
    }
}
