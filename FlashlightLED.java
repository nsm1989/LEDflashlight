package com.advback.ledflashlight;

import java.lang.reflect.Method;

import android.os.IBinder;

public class FlashlightLED {

        private Object service = null;
        private Method getFlashlightEnabled = null;
        private Method setFlashlightEnabled = null;
        
        @SuppressWarnings("unchecked")
        public FlashlightLED() throws Exception {
                try {
                        // call ServiceManager.getService("hardware") to get an IBinder for the service.
                        // this appears to be totally undocumented and not exposed in the SDK whatsoever.
                        Class serviceManager = Class.forName("android.os.ServiceManager");
                        Object hardwareBinder = serviceManager.getMethod("getService", String.class).invoke(null, "hardware");
                        
                        // get the hardware service stub. this seems to just get us one step closer to the proxy
                        Class hardwareServiceStub = Class.forName("android.os.IHardwareService$Stub");
                        Method asInterface = hardwareServiceStub.getMethod("asInterface", android.os.IBinder.class);
                        service = asInterface.invoke(null, (IBinder) hardwareBinder);
        
                        // grab the class (android.os.IHardwareService$Stub$Proxy) so we can reflect on its methods
                        Class proxy = service.getClass();
                        
                        // save the methods
                        getFlashlightEnabled = proxy.getMethod("getFlashlightEnabled");
                        setFlashlightEnabled = proxy.getMethod("setFlashlightEnabled", boolean.class);
                }
                catch(Exception e) {
                        throw new Exception("LEDs could not be initialized, device not supported");
                }
        }
        
        public boolean isEnabled() {
                try {
                        return getFlashlightEnabled.invoke(service).equals(true);
                }
                catch(Exception e) {
                        return false;
                }
        }
        
        public void enable(boolean enable) {
                try {
                        setFlashlightEnabled.invoke(service, enable);
                }
                catch(Exception e) {}
        }
}
