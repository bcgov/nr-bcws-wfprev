package ca.bc.gov.nrs.wfprev.interfaces;

import com.sun.jna.FunctionMapper;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.Library;
import com.sun.jna.win32.StdCallLibrary;

import java.lang.reflect.Method;
import java.util.HashMap;

public interface FileGDBLibrary extends StdCallLibrary {
    // Use explicit function mapping
    FileGDBLibrary INSTANCE = (FileGDBLibrary) Native.load("FileGDBAPI", FileGDBLibrary.class,
                    new HashMap<String, Object>() {{
                        put(OPTION_FUNCTION_MAPPER, new FunctionMapper() {
                            @Override
                            public String getFunctionName(NativeLibrary library, Method method) {
                                if (method.getName().equals("OpenGeodatabase")) {
                                    return "?OpenGeodatabase@FileGDBAPI@@YAHAEBV?$basic_string@_WU?$char_traits@_W@std@@V?$allocator@_W@2@@std@@AEAVGeodatabase@1@@Z";
                                }else if(method.getName().equals("CloseGeodatabase")) {
                                    return "?CloseGeodatabase@FileGDBAPI@@YAHAEAVGeodatabase@1@@Z";
                                }
                                return method.getName();
                            }
                        });
                    }}
    );

    // Exact function signatures matching the native library
    int OpenGeodatabase(WString path, PointerByReference geodatabase);
    int CloseGeodatabase(PointerByReference geodatabase);
    int GetTableNames(Pointer geodatabase, PointerByReference tableNames);
}