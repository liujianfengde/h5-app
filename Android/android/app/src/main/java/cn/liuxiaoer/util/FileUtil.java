package cn.liuxiaoer.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileUtil {

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "cn.liuxiaoer/";

    /**
     * 获取图片类型
     *
     * @param filePath
     * @return
     */
    public static String getFileType(String filePath) {
        HashMap<String, String> mFileTypes = new HashMap<String, String>();
        //images
        mFileTypes.put("FFD8FF", "jpg");
        mFileTypes.put("89504E47", "png");
        mFileTypes.put("47494638", "gif");
        mFileTypes.put("49492A00", "tif");
        mFileTypes.put("424D", "bmp");
        //
        mFileTypes.put("41433130", "dwg"); //CAD
        mFileTypes.put("38425053", "psd");
        mFileTypes.put("7B5C727466", "rtf"); //日记本
        mFileTypes.put("3C3F786D6C", "xml");
        mFileTypes.put("68746D6C3E", "html");
        mFileTypes.put("44656C69766572792D646174653A", "eml"); //邮件
        mFileTypes.put("D0CF11E0", "doc");
        mFileTypes.put("5374616E64617264204A", "mdb");
        mFileTypes.put("252150532D41646F6265", "ps");
        mFileTypes.put("255044462D312E", "pdf");
        mFileTypes.put("504B0304", "zip");
        mFileTypes.put("52617221", "rar");
        mFileTypes.put("57415645", "wav");
        mFileTypes.put("41564920", "avi");
        mFileTypes.put("2E524D46", "rm");
        mFileTypes.put("000001BA", "mpg");
        mFileTypes.put("000001B3", "mpg");
        mFileTypes.put("6D6F6F76", "mov");
        mFileTypes.put("3026B2758E66CF11", "asf");
        mFileTypes.put("4D546864", "mid");
        mFileTypes.put("1F8B08", "gz");

        return mFileTypes.get(getFileHeader(filePath));
    }

    /**
     * 获取文件头信息
     *
     * @param filePath
     * @return
     */
    public static String getFileHeader(String filePath) {
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(filePath);
            byte[] b = new byte[3];
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }

    /**
     * 将byte字节转换为十六进制字符串
     *
     * @param src
     * @return
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }

    public static final Map<String, String> MIMES =
            new HashMap<String, String>() {
                {
                    put(".apk", "application/vnd.android.package-archive");
                    put(".bin", "application/octet-stream");
                    put(".bmp", "image/bmp");
                    put(".c", "text/plain");
                    put(".class", "application/octet-stream");
                    put(".conf", "text/plain");
                    put(".cpp", "text/plain");
                    put(".doc", "application/msword");
                    put(".exe", "application/octet-stream");
                    put(".gtar", "application/x-gtar");
                    put(".gz", "application/x-gzip");
                    put(".h", "text/plain");
                    put(".jar", "application/java-archive");
                    put(".java", "text/plain");
                    put(".log", "text/plain");
                    put(".pdf", "application/pdf");
                    put(".pps", "application/vnd.ms-powerpoint");
                    put(".ppt", "application/vnd.ms-powerpoint");
                    put(".prop", "text/plain");
                    put(".rar", "application/x-rar-compressed");
                    put(".rc", "text/plain");
                    put(".rtf", "application/rtf");
                    put(".sh", "text/plain");
                    put(".tar", "application/x-tar");
                    put(".tgz", "application/x-compressed");
                    put(".txt", "text/plain");
                    put(".wps", "application/vnd.ms-works");
                    put(".z", "application/x-compress");
                    put(".zip", "application/zip");
                }
            };

    public static String getMIMEType(File file) {

        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0)
            return type;
        /* 获取文件的后缀名 */
        String fileType = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (fileType == null || "".equals(fileType))
            return type;

        return MIMES.get(type);
    }

    public static List<FileGroup> packageFileGroup(Context context) {
        List<FileGroup> groups = new ArrayList<>();
        Map<String, List<Member>> groupMap = new HashMap<>();
        File dir = new File(DOWNLOAD_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File[] files = dir.listFiles();
        for (File file : files) {
            long lastModified = file.lastModified();
            String date = DateUtils.formatDate(lastModified);
            List<Member> members = groupMap.get(date);
            long totalSpace = fileSize(file);
            if (members == null) {
                members = new ArrayList<>();
                Member member = new Member();
                member.setName(file.getName());
                member.setSize(formatFileSize(totalSpace));
                members.add(member);
                groupMap.put(date, members);
            } else {
                Member member = new Member();
                member.setName(file.getName());
                member.setSize(formatFileSize(totalSpace));
                members.add(member);
            }
        }

        Set<String> dates = groupMap.keySet();
        Iterator<String> dateIterator = dates.iterator();
        while (dateIterator.hasNext()) {
            String date = dateIterator.next();
            FileGroup group = new FileGroup();
            group.setName(date);
            group.setMembers(groupMap.get(date));
            groups.add(group);
        }

        Collections.sort(groups, new Comparator<FileGroup>() {
            @Override
            public int compare(FileGroup o1, FileGroup o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return groups;
    }

    private static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    private static long fileSize(File file) {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
            } catch (Exception e) {

            }
        }
        return size;
    }

    public static String getFileName(String path) {
        try {
            path = path.split("\\?")[0];
            path = path.substring(path.lastIndexOf("/") + 1);
        } catch (Exception e) {
            return "unknown";
        }
        return path;
    }

    public static String getFileExtensionFromPath(String path) {
        try {
            path = path.split("\\?")[0];
            path = path.substring(path.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "unknown";
        }
        return path;
    }
}