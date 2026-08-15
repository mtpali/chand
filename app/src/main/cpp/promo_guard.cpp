#include <jni.h>
#include <cstdint>
#include <fstream>
#include <string>
#include <vector>

namespace {

struct F {
    const uint8_t* p;
    size_t n;
    uint32_t s;
};

static const uint8_t q0[]  = {113,212,36,15,228,109,245,153,246,227,172,224,182,30,13,209,110,194,220,195,166,229,89,64,216,212,207,105,200,255,72,181,148,62,225,143,90,85,166,140,224,73};
static const uint8_t q1[]  = {252,28,217,254,215,248,158,194,68,2,15,10,27,80,230,241,60,167,242,70,203,8,105,188,244,204,137,36,62,192,240,66,151,164};
static const uint8_t q2[]  = {241,11,196,167,101,239,65,101,186,38,121};
static const uint8_t q3[]  = {208,53,145,6,45,111,57,36,209,62,158,6};
static const uint8_t q4[]  = {69,192,0,172,65,233,110,177,187,183,156,139};
static const uint8_t q5[]  = {32,36,163,78,255,40};
static const uint8_t q6[]  = {18,253,183,226,162,85,92,134,6,224,169,137,211,14,21,157,99,54,192,36,84};
static const uint8_t q7[]  = {125,55,55,92,66,202,226,4,78,162,106,34,214,24,81,133,26,169,193,228,136,27,227,192,9,81};
static const uint8_t q8[]  = {16,240,182,20,66,45,78,194,67,164,92,65,91,243,23,2,168,33,121,169,98,40,41,116,201,142};
static const uint8_t q9[]  = {158,133,87,27,148,71,189,10,165,19,125,108,44,8,234,89,136,177,209,252};
static const uint8_t q10[] = {185,94,93,23,75,177,85,18,54,48,155,213,96};
static const uint8_t q11[] = {73,195,136,51,6,251,6,153,185,113,123,121,182,79,51,73};
static const uint8_t q12[] = {104,146,54,110,160,12,70,122,240,142,131,183,103,51,230,33,208,51,213,89};
static const uint8_t q13[] = {190};
static const uint8_t q14[] = {230,212,197,175,126};

uint32_t m(uint32_t x) {
    x ^= x >> 16;
    x *= 0x7feb352dU;
    x ^= x >> 15;
    x *= 0x846ca68bU;
    x ^= x >> 16;
    return x;
}

uint8_t rr(uint8_t v, uint32_t r) {
    r &= 7U;
    if (r == 0U) return v;
    return static_cast<uint8_t>((v >> r) | (v << (8U - r)));
}

bool f(int i, F& out) {
    switch (i) {
        case 0:  out = {q0,  sizeof(q0),  0xA3C59AC3U}; return true;
        case 1:  out = {q1,  sizeof(q1),  0x7F4A7C15U}; return true;
        case 2:  out = {q2,  sizeof(q2),  0x91E10DA5U}; return true;
        case 3:  out = {q3,  sizeof(q3),  0xC2B2AE35U}; return true;
        case 4:  out = {q4,  sizeof(q4),  0x165667B1U}; return true;
        case 5:  out = {q5,  sizeof(q5),  0xD3A2646CU}; return true;
        case 6:  out = {q6,  sizeof(q6),  0xFD7046C5U}; return true;
        case 7:  out = {q7,  sizeof(q7),  0xB55A4F09U}; return true;
        case 8:  out = {q8,  sizeof(q8),  0x8D12E6B7U}; return true;
        case 9:  out = {q9,  sizeof(q9),  0xE12398AFU}; return true;
        case 10: out = {q10, sizeof(q10), 0x5F356495U}; return true;
        case 11: out = {q11, sizeof(q11), 0x9E3779B9U}; return true;
        case 12: out = {q12, sizeof(q12), 0x31415926U}; return true;
        case 13: out = {q13, sizeof(q13), 0x27182818U}; return true;
        case 14: out = {q14, sizeof(q14), 0xA5A5C3C3U}; return true;
        default: return false;
    }
}

std::vector<uint8_t> d(const F& x) {
    std::vector<uint8_t> out(x.n);
    for (size_t i = 0; i < x.n; ++i) {
        const uint32_t k = m(x.s + static_cast<uint32_t>(i) * 0x9e3779b9U);
        uint8_t v = static_cast<uint8_t>(x.p[i] - static_cast<uint8_t>((k >> 8) & 0xffU));
        v ^= static_cast<uint8_t>(k & 0xffU);
        out[i] = rr(v, (k >> 16) & 7U);
    }
    return out;
}

std::string s(int slot) {
    F x{};
    if (!f(slot, x)) return {};
    const auto b = d(x);
    return std::string(reinterpret_cast<const char*>(b.data()), b.size());
}

bool ok() {
    std::ifstream in("/proc/self/cmdline", std::ios::in | std::ios::binary);
    if (!in.good()) return false;

    std::string p;
    std::getline(in, p, '\0');
    return p == s(11);
}

jbyteArray empty(JNIEnv* env) {
    return env->NewByteArray(0);
}

jbyteArray nv(JNIEnv* env, jobject, jint slot) {
    if (!ok() || slot < 0 || slot > 10) return empty(env);

    F x{};
    if (!f(slot, x)) return empty(env);
    const auto b = d(x);

    jbyteArray out = env->NewByteArray(static_cast<jsize>(b.size()));
    if (out == nullptr || b.empty()) return out;
    env->SetByteArrayRegion(
        out,
        0,
        static_cast<jsize>(b.size()),
        reinterpret_cast<const jbyte*>(b.data())
    );
    return out;
}

} // namespace

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
        return JNI_ERR;
    }

    const std::string clsName = s(12);
    const std::string methodName = s(13);
    const std::string signature = s(14);
    if (clsName.empty() || methodName.empty() || signature.empty()) return JNI_ERR;

    jclass cls = env->FindClass(clsName.c_str());
    if (cls == nullptr) return JNI_ERR;

    JNINativeMethod methods[] = {
        {
            const_cast<char*>(methodName.c_str()),
            const_cast<char*>(signature.c_str()),
            reinterpret_cast<void*>(nv)
        }
    };

    if (env->RegisterNatives(cls, methods, 1) != JNI_OK) return JNI_ERR;
    return JNI_VERSION_1_6;
}
