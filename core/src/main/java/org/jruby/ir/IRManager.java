package org.jruby.ir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jruby.RubyInstanceConfig;
import org.jruby.ir.listeners.IRScopeListener;
import org.jruby.ir.listeners.InstructionsListener;
import org.jruby.ir.operands.Nil;
import org.jruby.ir.passes.BasicCompilerPassListener;
import org.jruby.ir.passes.CompilerPass;
import org.jruby.ir.passes.CompilerPassListener;
import org.jruby.ir.passes.CompilerPassScheduler;

/**
 */
public class IRManager {
    public static String SAFE_COMPILER_PASSES = "LinearizeCFG";
    public static String DEFAULT_COMPILER_PASSES = "OptimizeTempVarsPass,LocalOptimizationPass,AddLocalVarLoadStoreInstructions,EnsureTempsAssigned,LinearizeCFG";
    public static String DEFAULT_JIT_PASSES = "OptimizeTempVarsPass,LocalOptimizationPass,AddLocalVarLoadStoreInstructions,EnsureTempsAssigned,AddCallProtocolInstructions,LinearizeCFG";
    public static String DEFAULT_INLINING_COMPILER_PASSES = "LocalOptimizationPass";

    private int dummyMetaClassCount = 0;
    private final IRModuleBody object = new IRClassBody(this, null, "Object", "", 0, null);
    private final Nil nil = new Nil();
    private final org.jruby.ir.operands.Boolean trueObject = new org.jruby.ir.operands.Boolean(true);
    private final org.jruby.ir.operands.Boolean falseObject = new org.jruby.ir.operands.Boolean(false);
    // Listeners for debugging and testing of IR
    private Set<CompilerPassListener> passListeners = new HashSet<CompilerPassListener>();
    private CompilerPassListener defaultListener = new BasicCompilerPassListener();

    private InstructionsListener instrsListener = null;
    private IRScopeListener irScopeListener = null;


    // FIXME: Eventually make these attrs into either a) set b) part of state machine
    private List<CompilerPass> compilerPasses;
    private List<CompilerPass> inliningCompilerPasses;
    private List<CompilerPass> jitPasses;
    private List<CompilerPass> safePasses;

    // If true then code will not execute (see ir/ast tool)
    private boolean dryRun = false;

    public IRManager() {
        compilerPasses = CompilerPass.getPassesFromString(RubyInstanceConfig.IR_COMPILER_PASSES, DEFAULT_COMPILER_PASSES);
        inliningCompilerPasses = CompilerPass.getPassesFromString(RubyInstanceConfig.IR_COMPILER_PASSES, DEFAULT_INLINING_COMPILER_PASSES);
        jitPasses = CompilerPass.getPassesFromString(RubyInstanceConfig.IR_JIT_PASSES, DEFAULT_JIT_PASSES);
        safePasses = CompilerPass.getPassesFromString(null, SAFE_COMPILER_PASSES);
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean value) {
        this.dryRun = value;
    }

    public Nil getNil() {
        return nil;
    }

    public org.jruby.ir.operands.Boolean getTrue() {
        return trueObject;
    }

    public org.jruby.ir.operands.Boolean getFalse() {
        return falseObject;
    }

    public IRModuleBody getObject() {
        return object;
    }

    public CompilerPassScheduler schedulePasses() {
        return schedulePasses(compilerPasses);
    }

    public static CompilerPassScheduler schedulePasses(final List<CompilerPass> passes) {
        CompilerPassScheduler scheduler = new CompilerPassScheduler() {
            private Iterator<CompilerPass> iterator;
            {
                this.iterator = passes.iterator();
            }

            @Override
            public Iterator<CompilerPass> iterator() {
                return this.iterator;
            }

        };
        return scheduler;
    }

    public List<CompilerPass> getCompilerPasses(IRScope scope) {
        return compilerPasses;
    }

    public List<CompilerPass> getInliningCompilerPasses(IRScope scope) {
        return inliningCompilerPasses;
    }

    public List<CompilerPass> getJITPasses(IRScope scope) {
        return jitPasses;
    }

    public List<CompilerPass> getSafePasses(IRScope scope) {
        return safePasses;
    }

    public Set<CompilerPassListener> getListeners() {
        // FIXME: This is ugly but we want to conditionalize output based on JRuby module setting/unsetting
        if (RubyInstanceConfig.IR_COMPILER_DEBUG) {
            addListener(defaultListener);
        } else {
            removeListener(defaultListener);
        }

        return passListeners;
    }

    public InstructionsListener getInstructionsListener() {
        return instrsListener;
    }

    public IRScopeListener getIRScopeListener() {
        return irScopeListener;
    }

    public void addListener(CompilerPassListener listener) {
        passListeners.add(listener);
    }

    public void removeListener(CompilerPassListener listener) {
        passListeners.remove(listener);
    }

    public void addListener(InstructionsListener listener) {
        if (RubyInstanceConfig.IR_COMPILER_DEBUG || RubyInstanceConfig.IR_VISUALIZER) {
            if (instrsListener != null) {
                throw new RuntimeException("InstructionsListener is set and other are currently not allowed");
            }

            instrsListener = listener;
        }
    }

    public void removeListener(InstructionsListener listener) {
        if (instrsListener.equals(listener)) instrsListener = null;
    }

    public void addListener(IRScopeListener listener) {
        if (RubyInstanceConfig.IR_COMPILER_DEBUG || RubyInstanceConfig.IR_VISUALIZER) {
            if (irScopeListener != null) {
                throw new RuntimeException("IRScopeListener is set and other are currently not allowed");
            }

            irScopeListener = listener;
        }
    }

    public void removeListener(IRScopeListener listener) {
        if (irScopeListener.equals(listener)) irScopeListener = null;
    }

    public String getMetaClassName() {
        return "<DUMMY_MC:" + dummyMetaClassCount++ + ">";
    }
}
