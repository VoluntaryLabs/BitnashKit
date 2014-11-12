//
//  BNJavaInstall.m
//  BitnashKit
//
//  Created by Steve Dekorte on 11/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNJavaInstall.h"

@implementation BNJavaInstall

- (BOOL)isInstalled
{
    NSString *testPath = @"/System/Library/Java/JavaVirtualMachines";
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:testPath] &&
        [[NSFileManager defaultManager] subpathsAtPath:testPath].count > 0)
    {
        return YES;
    }
    
    return NO;
}

/*
 
- (NSString *)javaExePath
{
    return @"/usr/bin/java";
}

 
- (BOOL)isInstalled
{
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.javaExePath])
    {
        return NO;
    }
    
    NSTask *task = [[NSTask alloc] init];
    [task setLaunchPath:self.javaExePath];
    
    NSPipe *outPipe = [NSPipe pipe];
    
    [task setStandardInput: [NSFileHandle fileHandleWithNullDevice]];
    [task setStandardOutput:outPipe];
    [task setStandardError:outPipe];
    
    NSMutableArray *args = [NSMutableArray array];
    [args addObject:@"-version"];
    [task setArguments:args];
    
    @try
    {
        [task launch];
        [task waitUntilExit];
    }
    @catch (NSException *exception)
    {
        NSLog(@"%@", exception);
        [task terminate];
        return NO;
    }
    
    NSData *theData = [outPipe fileHandleForReading].availableData;
    NSString *result = [[NSString alloc] initWithData:theData encoding:NSUTF8StringEncoding];
    
    return [result hasPrefix:@"java version"];
}

- (void)presentInstaller
{
    NSTask *task = [[NSTask alloc] init];
    [task setLaunchPath:self.javaExePath];
    [task launch];
}
 */

- (void)openJavaDmg
{
    if (self.javaEmbeddedDmgFilePath)
    {
        [[NSWorkspace sharedWorkspace] openFile:self.javaEmbeddedDmgFilePath];
    }
    else
    {
        [[NSWorkspace sharedWorkspace] openURL:self.javaDmgURL];
    }
}

- (NSString *)javaEmbeddedDmgFilePath
{
    return [[NSBundle bundleForClass:self.class]
                          pathForResource:@"JavaForOSX2014-001.dmg"
                          ofType:nil];
}

- (NSURL *)javaDmgURL
{
     return [NSURL URLWithString:@"http://support.apple.com/downloads/DL1572/en_US/JavaForOSX2014-001.dmg"];
}

@end
