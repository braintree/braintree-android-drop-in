package com.braintreepayments.demo.internal;

//public class LeakLoggerService extends DisplayLeakService {
//
//    public static void setupLeakCanary(Application application) {
//        LeakCanary.install(application, LeakLoggerService.class,
//                AndroidExcludedRefs.createAppDefaults().build());
//    }
//
//    @Override
//    protected void afterDefaultHandling(HeapDump heapDump, AnalysisResult result, String leakInfo) {
//        if (!result.leakFound || result.excludedLeak) {
//            return;
//        }
//
//        Log.w("LeakCanary", leakInfo);
//    }
//}
