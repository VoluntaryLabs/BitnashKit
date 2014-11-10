//
//  BNJavaInstall.m
//  BitnashKit
//
//  Created by Steve Dekorte on 11/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNJavaInstall.h"

@implementation BNJavaInstall

- (NSString *)javaExePath
{
    return @"/usr/bin/java";
}

- (BOOL)isJavaInstalled
{
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.self.javaExePath])
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

@end
