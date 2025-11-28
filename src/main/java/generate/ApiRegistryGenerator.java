package generate;

import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ApiRegistryGenerator {

    private static final String REGISTRY_PATH =
            System.getProperty("user.dir") + "/src/test/java/framework/core/ApiRegistry.java";

    public void addRegistryEntry(String module, String className, String registryKey) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(REGISTRY_PATH));

        String groupName = module.replace("/", ".");
        String classFullPath = "tests.test_scripts.api." + module + "." + className;

        String newEntry = "        put(\"" + registryKey + "\", \"" + classFullPath + "\");";

        // 1) Check if key already exists → return
        for (String line : lines) {
            if (line.contains("put(\"" + registryKey + "\"")) {
                System.out.println("⚠️ Registry exists → skip: " + registryKey);
                return;
            }
        }

        // 2) Find group comment:  // ===== booking.create_booking =====
        Pattern groupPattern = Pattern.compile("^\\s*// ===== " + Pattern.quote(groupName) + " =====");
        int groupLineIndex = -1;

        for (int i = 0; i < lines.size(); i++) {
            if (groupPattern.matcher(lines.get(i)).find()) {
                groupLineIndex = i;
                break;
            }
        }

        if (groupLineIndex != -1) {
            // 3) Insert into existing group → find the next blank/comment or next group
            int insertIndex = groupLineIndex + 1;

            for (int i = groupLineIndex + 1; i < lines.size(); i++) {
                String l = lines.get(i).trim();

                // Nếu gặp group mới
                if (l.startsWith("// =====") && !l.contains(groupName)) {
                    insertIndex = i;
                    break;
                }

                // Nếu gặp dấu đóng MAP
                if (l.startsWith("}")) {
                    insertIndex = i;
                    break;
                }
            }

            lines.add(insertIndex, newEntry);
        } else {
            // 4) Group not exists → insert before MAP closing '}};'
            int insertIndex = -1;

            for (int i = lines.size() - 1; i >= 0; i--) {
                if (lines.get(i).contains("}};")) {
                    insertIndex = i;
                    break;
                }
            }

            if (insertIndex == -1) {
                throw new RuntimeException("❌ Không tìm thấy kết thúc MAP trong ApiRegistry.java");
            }

            // Add new group
            lines.add(insertIndex, "        // ===== " + groupName + " =====");
            lines.add(insertIndex + 1, newEntry);
        }

        // 5) Write file back without touching formatting
        Files.write(Paths.get(REGISTRY_PATH), lines);

        System.out.println("✔ Appended to ApiRegistry: " + registryKey);
    }
}
